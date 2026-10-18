package ext.library.oss;

import ext.library.oss.config.OssProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * S3 协议对象存储
 * <p>
 * 适用于 AWS S3、MinIO、阿里云 OSS 等一切兼容 S3 的服务。
 *
 * @since 4.1.0
 */
public class S3ObjectStorage implements ObjectStorage {

    /**
     * S3 协议里代表对象/存储桶不存在的错误码
     */
    private static final Set<String> NOT_FOUND_CODES = Set.of("NoSuchKey", "NoSuchObject", "NoSuchBucket");

    private final MinioClient client;

    private final OssProperties properties;

    private final String bucket;

    /**
     * 构造 S3 存储
     *
     * @param client     MinIO 客户端
     * @param properties 配置
     */
    public S3ObjectStorage(MinioClient client, OssProperties properties) {
        this.client = client;
        this.properties = properties;
        this.bucket = properties.getBucket();
        if (properties.isAutoCreateBucket()) {
            createBucket();
        }
    }

    /**
     * 存储桶不存在时创建
     */
    private void createBucket() {
        try {
            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (MinioException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "创建存储桶失败：{}", bucket);
        }
    }

    @Override
    public String name() {
        return "s3";
    }

    @Override
    public void put(String key, InputStream content, long size, String contentType) {
        try {
            // size 非正数时传 -1，表示长度未知，由 SDK 边读边分片
            long streamSize = size > 0 ? size : -1L;
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(content, streamSize, properties.getPartSize())
                    .contentType(contentType)
                    .build());
        } catch (MinioException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "写入对象失败：{}", key);
        }
    }

    @Override
    public OssObject get(String key) {
        try {
            var stat = client.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            GetObjectResponse response = client.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
            return new OssObject(bucket, key, stat.contentType(), stat.size(), response);
        } catch (MinioException e) {
            if (notFound(e)) {
                throw new ExtException(EmojiSymbol.OSS, "对象不存在：{}", key);
            }
            throw new ExtException(EmojiSymbol.OSS, e, "读取对象失败：{}", key);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
            return true;
        } catch (MinioException e) {
            if (notFound(e)) {
                return false;
            }
            throw new ExtException(EmojiSymbol.OSS, e, "删除对象失败：{}", key);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(key).build());
            return true;
        } catch (MinioException e) {
            if (notFound(e)) {
                return false;
            }
            throw new ExtException(EmojiSymbol.OSS, e, "查询对象失败：{}", key);
        }
    }

    @Override
    public List<ObjectItem> list(String prefix) {
        List<ObjectItem> items = new ArrayList<>();
        try {
            for (Result<Item> result : client.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .recursive(true)
                    .build())) {
                Item item = result.get();
                if (!item.isDir()) {
                    items.add(new ObjectItem(item.objectName(), item.size(), item.lastModified().toString()));
                }
            }
        } catch (MinioException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "列举对象失败：{}", prefix);
        }
        return items.stream().sorted(Comparator.comparing(ObjectItem::key)).toList();
    }

    @Override
    public String presign(String key, Duration expiry) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET)
                    .bucket(bucket)
                    .object(key)
                    .expiry((int) expiry.toSeconds(), TimeUnit.SECONDS)
                    .build());
        } catch (MinioException e) {
            throw new ExtException(EmojiSymbol.OSS, e, "生成预签名链接失败：{}", key);
        }
    }

    /**
     * 判断异常是否为对象/存储桶不存在
     *
     * @param e 异常
     *
     * @return 不存在返回 {@code true}
     */
    private boolean notFound(Exception e) {
        return e instanceof ErrorResponseException response && NOT_FOUND_CODES.contains(response.errorResponse().code());
    }

}
