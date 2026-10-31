package ext.library.oss;

import com.adobe.testing.s3mock.junit5.S3MockExtension;
import ext.library.oss.config.OssProperties;
import ext.library.tool.exception.ExtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * S3 对象存储测试
 * <p>
 * 用 S3Mock 在进程内起一个 S3 服务端，走真实的 AWS SDK 往返（含分片上传与预签名链接），
 * 不需要 Docker。客户端与预签名器按 {@code OssAutoConfiguration} 的同一套参数构造，
 * 这样 path-style、chunked-encoding 这些开关的问题能在测试里暴露。
 */
@DisplayName("S3ObjectStorage 测试")
class S3ObjectStorageTest {

    private static final String ACCESS_KEY = "ext-access-key";

    private static final String SECRET_KEY = "ext-secret-key";

    private static final long PART_SIZE = 5L * 1024 * 1024;

    @RegisterExtension
    static final S3MockExtension S3_MOCK = S3MockExtension.builder().silent().withHttpPort(0)
            .withSecureConnection(false).build();

    private S3Client client;

    private S3Presigner presigner;

    private OssProperties properties;

    private S3ObjectStorage storage;

    private String bucket;

    @BeforeEach
    void setUp() {
        String endpoint = S3_MOCK.getServiceEndpoint();
        // 每个用例一个桶，省掉清理逻辑
        bucket = "ext-oss-" + UUID.randomUUID().toString().toLowerCase();

        properties = new OssProperties();
        properties.setBucket(bucket);
        properties.setEndpoint(endpoint);
        properties.setAccessKey(ACCESS_KEY);
        properties.setSecretKey(SECRET_KEY);
        properties.setRegion("us-east-1");
        properties.setPathStyleAccess(true);
        properties.setChunkedEncoding(true);
        properties.setPartSize(PART_SIZE);

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                .chunkedEncodingEnabled(properties.isChunkedEncoding())
                .build();
        StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY));

        client = S3Client.builder()
                .credentialsProvider(credentials)
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(serviceConfiguration)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentials)
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(serviceConfiguration)
                .build();

        // auto-create-bucket 默认开启，构造时就把桶建好
        storage = new S3ObjectStorage(client, presigner, properties);
    }

    @AfterEach
    void tearDown() {
        presigner.close();
        client.close();
    }

    @Test
    @DisplayName("构造时自动创建存储桶")
    void testAutoCreateBucket() {
        // 桶不存在时 headBucket 会报 NoSuchBucket，能查通就说明建好了
        client.headBucket(builder -> builder.bucket(bucket));
        assertFalse(storage.exists("anything"));
    }

    @Test
    @DisplayName("写入后能读回内容与元信息")
    void testPutAndGet() throws Exception {
        storage.put("avatar/logo.txt", stream("hello"), 5, "text/plain");

        assertTrue(storage.exists("avatar/logo.txt"));
        ObjectMeta meta = storage.stat("avatar/logo.txt");
        assertEquals(5, meta.size());
        assertEquals("text/plain", meta.contentType());
        assertFalse(meta.lastModified().isEmpty());
        try (OssObject object = storage.get("avatar/logo.txt")) {
            assertEquals("hello", new String(object.content().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("超过分片阈值的对象走分片上传且内容完整")
    void testMultipartPutWithKnownSize() throws Exception {
        byte[] payload = payload(PART_SIZE + 1024);

        storage.put("big/blob.bin", new ByteArrayInputStream(payload), payload.length, "application/octet-stream");

        assertEquals(payload.length, storage.stat("big/blob.bin").size());
        try (OssObject object = storage.get("big/blob.bin")) {
            assertArrayEquals(payload, object.content().readAllBytes());
        }
    }

    @Test
    @DisplayName("长度未知的流同样能分片上传")
    void testMultipartPutWithUnknownSize() throws Exception {
        byte[] payload = payload(PART_SIZE + 1024);

        storage.put("big/unknown.bin", new ByteArrayInputStream(payload), -1, "application/octet-stream");

        assertEquals(payload.length, storage.stat("big/unknown.bin").size());
        try (OssObject object = storage.get("big/unknown.bin")) {
            assertArrayEquals(payload, object.content().readAllBytes());
        }
    }

    @Test
    @DisplayName("空流写入不报错")
    void testPutEmpty() {
        storage.put("empty.txt", stream(""), 0, "text/plain");

        assertTrue(storage.exists("empty.txt"));
        assertEquals(0, storage.stat("empty.txt").size());
    }

    @Test
    @DisplayName("长度未知的空流写入不报错")
    void testPutEmptyWithUnknownSize() {
        storage.put("empty-unknown.txt", stream(""), -1, "text/plain");

        assertTrue(storage.exists("empty-unknown.txt"));
        assertEquals(0, storage.stat("empty-unknown.txt").size());
    }

    @Test
    @DisplayName("同桶与跨桶复制都保留��对象")
    void testCopy() {
        storage.put("a.txt", stream("data"), 4, "text/plain");

        storage.copy("a.txt", "dir/a.txt");
        assertTrue(storage.exists("a.txt"));
        assertTrue(storage.exists("dir/a.txt"));

        String otherBucket = bucket + "-other";
        client.createBucket(CreateBucketRequest.builder().bucket(otherBucket).build());
        storage.copy(bucket, "a.txt", otherBucket, "copied.txt");
        assertTrue(storage.exists("a.txt"));
        assertEquals(4, client.headObject(builder -> builder.bucket(otherBucket).key("copied.txt")).contentLength());
    }

    @Test
    @DisplayName("列举按前缀过滤并按 key 排序")
    void testList() {
        storage.put("img/b.png", stream("b"), 1, "image/png");
        storage.put("img/a.png", stream("a"), 1, "image/png");
        storage.put("doc/c.txt", stream("c"), 1, "text/plain");

        List<ObjectItem> items = storage.list("img/");

        assertEquals(2, items.size());
        assertEquals("img/a.png", items.get(0).key());
        assertEquals("img/b.png", items.get(1).key());
    }

    @Test
    @DisplayName("删除返回对象是否存在")
    void testDelete() {
        storage.put("x.txt", stream("x"), 1, "text/plain");

        assertTrue(storage.delete("x.txt"));
        assertFalse(storage.delete("x.txt"));
    }

    @Test
    @DisplayName("读取不存在的对象报业务异常")
    void testGetMissing() {
        assertThrows(ExtException.class, () -> storage.get("missing.txt"));
        assertThrows(ExtException.class, () -> storage.stat("missing.txt"));
        assertFalse(storage.exists("missing.txt"));
    }

    @Test
    @DisplayName("预签名下载链接可直接取到对象")
    void testPresignGet() throws Exception {
        storage.put("signed.txt", stream("signed-body"), 11, "text/plain");

        String url = storage.presign("signed.txt", Duration.ofMinutes(5));

        assertTrue(url.contains("X-Amz-Signature"), url);
        HttpResponse<String> response = httpGet(url);
        assertEquals(200, response.statusCode());
        assertEquals("signed-body", response.body());
    }

    @Test
    @DisplayName("预签名上传链接可直接写入对象")
    void testPresignPut() throws Exception {
        String url = storage.presignPut("uploaded.txt", Duration.ofMinutes(5));

        assertTrue(url.contains("X-Amz-Signature"), url);
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url))
                        .PUT(HttpRequest.BodyPublishers.ofString("from-presigned-put"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());

        assertTrue(storage.exists("uploaded.txt"));
        try (OssObject object = storage.get("uploaded.txt")) {
            assertEquals("from-presigned-put", new String(object.content().readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("桶不存在时读取报业务异常")
    void testMissingBucket() {
        properties.setBucket(bucket + "-absent");
        properties.setAutoCreateBucket(false);
        S3ObjectStorage absent = new S3ObjectStorage(client, presigner, properties);

        assertThrows(ExtException.class, () -> absent.get("a.txt"));
        assertThrows(NoSuchBucketException.class, () -> client.headBucket(builder -> builder.bucket(bucket + "-absent")));
    }

    private HttpResponse<String> httpGet(String url) throws Exception {
        return HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private byte[] payload(long size) {
        byte[] payload = new byte[(int) size];
        java.util.Arrays.fill(payload, (byte) 'x');
        byte[] marker = "END".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(marker, 0, payload, payload.length - marker.length, marker.length);
        return payload;
    }

    private InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

}
