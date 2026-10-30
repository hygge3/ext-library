package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * S3 协议对象存储
 * <p>
 * 基于 AWS SDK for Java v2，适用于 AWS S3 以及 MinIO、Ceph、Cloudflare R2、阿里云 OSS 等一切兼容 S3 的服务；
 * 自建端点通常要靠 {@code ext.oss.path-style-access} 才能访问。
 * <p>
 * 构造参数用 {@link Object} 收 SDK 类型：{@code S3Client} / {@code S3Presigner} 由自动配置类在方法体内造好传进来，
 * 这样本类的签名上不出现厂商类型，没引 SDK 时也不会被类加载器碰到。
 *
 * @since 4.1.0
 */
public class S3ObjectStorage implements ObjectStorage {

    /**
     * S3 分片上传要求除最后一片外每片至少 5MB
     */
    private static final long MIN_PART_SIZE = 5L * 1024 * 1024;

    private final S3Client client;

    private final S3Presigner presigner;

    private final OssProperties properties;

    private final String bucket;

    /**
     * 构造 S3 存储
     *
     * @param client     {@code software.amazon.awssdk.services.s3.S3Client}
     * @param presigner  {@code software.amazon.awssdk.services.s3.presigner.S3Presigner}
     * @param properties 配置
     */
    public S3ObjectStorage(Object client, Object presigner, OssProperties properties) {
        this.client = (S3Client) client;
        this.presigner = (S3Presigner) presigner;
        this.properties = properties;
        this.bucket = properties.getBucket();
        if (properties.isAutoCreateBucket()) {
            createBucketIfAbsent();
        }
    }

    /**
     * 存储桶不存在时创建
     */
    private void createBucketIfAbsent() {
        try {
            client.headBucket(builder -> builder.bucket(bucket));
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw new ExtException(EmojiSymbol.OSS, e, "检查存储桶失败：{}", bucket);
            }
            try {
                client.createBucket(builder -> builder.bucket(bucket));
            } catch (S3Exception createError) {
                // 并发启动时两个实例同时建桶，后来者会收到 BucketAlreadyOwnedByYou，视为成功
                if (createError.statusCode() == 409) {
                    return;
                }
                throw new ExtException(EmojiSymbol.OSS, createError, "创建存储桶失败：{}", bucket);
            }
        }
    }

    @Override
    public String name() {
        return "s3";
    }

    @Override
    public void put(String key, InputStream content, long size, String contentType) {
        long partSize = Math.max(properties.getPartSize(), MIN_PART_SIZE);
        if (size >= 0 && size <= partSize) {
            // 一次装得下（含空对象），单请求直传
            try {
                client.putObject(
                        PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType)
                                .contentLength(size).build(),
                        RequestBody.fromInputStream(content, size)
                );
            } catch (S3Exception e) {
                throw new ExtException(EmojiSymbol.OSS, e, "写入对象失败：{}", key);
            }
            return;
        }
        // 大对象或长度未知：分片上传
        multipartPut(key, content, size, contentType, partSize);
    }

    /**
     * 分片上传
     * <p>
     * 先建 uploadId，逐片上传后 complete；中途失败一律 abort，避免留下收费的碎片。
     *
     * @param key         对象键
     * @param content     内容流
     * @param size        已知字节数，未知时传非正数
     * @param contentType 内容类型
     * @param partSize    分片大小
     */
    private void multipartPut(String key, InputStream content, long size, String contentType, long partSize) {
        String uploadId = null;
        try {
            uploadId = client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                    .bucket(bucket).key(key).contentType(contentType).build()).uploadId();

            List<CompletedPart> parts = new ArrayList<>();
            long remaining = size > 0 ? size : -1;
            int partNumber = 1;
            while (true) {
                long want = remaining > 0 ? Math.min(partSize, remaining) : partSize;
                byte[] chunk = readAtMost(content, (int) want);
                if (chunk.length == 0) {
                    break;
                }
                parts.add(uploadPart(key, uploadId, partNumber++, chunk));
                if (remaining > 0) {
                    remaining -= chunk.length;
                    if (remaining <= 0) {
                        break;
                    }
                } else if (chunk.length < want) {
                    // 长度未知时读到不满一片说明流已结束
                    break;
                }
            }

            if (parts.isEmpty()) {
                // 空流：分片上传不接受 0 片，先放弃它再补一个零长度的单请求
                abortQuietly(key, uploadId);
                uploadId = null;
                client.putObject(
                        PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).contentLength(0L).build(),
                        RequestBody.empty()
                );
                return;
            }

            client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                    .bucket(bucket).key(key).uploadId(uploadId)
                    .multipartUpload(CompletedMultipartUpload.builder().parts(parts).build())
                    .build());
            uploadId = null;
        } catch (S3Exception e) {
            throw new ExtException(EmojiSymbol.OSS, e, "写入对象失败：{}", key);
        } catch (IOException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "读取待上传内容失败：{}", key);
        } finally {
            if (uploadId != null) {
                abortQuietly(key, uploadId);
            }
        }
    }

    /**
     * 上传一片
     *
     * @param key           对象键
     * @param uploadId      上传 id
     * @param partNumber 分片序号，从 1 开始
     * @param chunk      分片内容
     *
     * @return 已完成的分片
     */
    private CompletedPart uploadPart(String key, String uploadId, int partNumber, byte[] chunk) {
        String eTag = client.uploadPart(UploadPartRequest.builder()
                        .bucket(bucket).key(key).uploadId(uploadId)
                        .partNumber(partNumber).contentLength((long) chunk.length).build(),
                RequestBody.fromBytes(chunk)).eTag();
        return CompletedPart.builder().partNumber(partNumber).eTag(eTag).build();
    }

    /**
     * 静默放弃分片上传
     *
     * @param key      对象键
     * @param uploadId 上传 id
     */
    private void abortQuietly(String key, String uploadId) {
        try {
            client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                    .bucket(bucket).key(key).uploadId(uploadId).build());
        } catch (RuntimeException e) {
            // 放弃失败不影响业务结果，留日志由 SDK 自己打
        }
    }

    /**
     * 从流中最多读 {@code max} 字节
     *
     * @param content 内容流
     * @param max     最多读取字节数
     *
     * @return 实际读到的内容，流已结束则为空数组
     *
     * @throws IOException 读取失败
     */
    private byte[] readAtMost(InputStream content, int max) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(max);
        byte[] chunk = new byte[8192];
        while (buffer.size() < max) {
            int read = content.read(chunk, 0, Math.min(chunk.length, max - buffer.size()));
            if (read < 0) {
                break;
            }
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    @Override
    public OssObject get(String key) {
        try {
            ResponseInputStream<GetObjectResponse> response = client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(key).build());
            GetObjectResponse meta = response.response();
            return new OssObject(bucket, key, meta.contentType(), meta.contentLength(), response);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", key);
            }
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象失败：{}", key);
        }
    }

    @Override
    public ObjectMeta stat(String key) {
        try {
            HeadObjectResponse head = client.headObject(
                    HeadObjectRequest.builder().bucket(bucket).key(key).build());
            String lastModified = head.lastModified() == null ? "" : head.lastModified().toString();
            return new ObjectMeta(bucket, key, head.contentLength(), head.contentType(), lastModified);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", key);
            }
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象信息失败：{}", key);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            if (!exists(key)) {
                return false;
            }
            client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (S3Exception e) {
            throw new ExtException(EmojiSymbol.OSS, e, "删除对象失败：{}", key);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new ExtException(EmojiSymbol.OSS, e, "查询对象失败：{}", key);
        }
    }

    @Override
    public List<ObjectItem> list(String prefix) {
        try {
            List<ObjectItem> items = new ArrayList<>();
            for (S3Object object : client.listObjectsV2Paginator(ListObjectsV2Request.builder()
                    .bucket(bucket).prefix(prefix).build()).contents()) {
                items.add(new ObjectItem(object.key(), object.size(), String.valueOf(object.lastModified())));
            }
            return items.stream().sorted(Comparator.comparing(ObjectItem::key)).toList();
        } catch (S3Exception e) {
            throw new ExtException(EmojiSymbol.OSS, e, "列举对象失败：{}", prefix);
        }
    }

    @Override
    public void copy(String sourceKey, String targetKey) {
        copy(bucket, sourceKey, bucket, targetKey);
    }

    @Override
    public void copy(String sourceBucket, String sourceKey, String targetBucket, String targetKey) {
        try {
            // 服务端复制：数据不经过应用进程，大对象也只有一次控制面调用
            client.copyObject(CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket).sourceKey(sourceKey)
                    .destinationBucket(targetBucket).destinationKey(targetKey)
                    .build());
        } catch (S3Exception e) {
            throw new ExtException(EmojiSymbol.OSS, e, "复制对象失败：{}/{} -> {}/{}",
                    sourceBucket, sourceKey, targetBucket, targetKey);
        }
    }

    @Override
    public String presign(String key, Duration expiry) {
        try {
            return presigner.presignGetObject(GetObjectPresignRequest.builder()
                            .signatureDuration(expiry)
                            .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
                            .build())
                    .url().toString();
        } catch (RuntimeException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "生成预签名链接失败：{}", key);
        }
    }

    @Override
    public String presignPut(String key, Duration expiry) {
        try {
            return presigner.presignPutObject(PutObjectPresignRequest.builder()
                            .signatureDuration(expiry)
                            .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(key).build())
                            .build())
                    .url().toString();
        } catch (RuntimeException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "生成预签名上传链接失败：{}", key);
        }
    }

}
