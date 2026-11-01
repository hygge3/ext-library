package ext.library.oss.config;

import ext.library.oss.StorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 对象存储配置
 * <p>
 * 前缀 {@code ext.oss}
 *
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = OssProperties.PREFIX)
public class OssProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "ext.oss";

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 存储类型
     */
    private StorageType type = StorageType.LOCAL;

    /**
     * 本地存储根目录
     */
    private String path = "./data/oss";

    /**
     * 本地存储对外访问前缀（为空时 {@code presign} 直接报错）
     */
    private String urlPrefix = "";

    /**
     * S3 服务地址，如 {@code https://s3.example.com}；对接 AWS 官方端点时可留空
     */
    private String endpoint;

    /**
     * S3 访问密钥
     */
    private String accessKey;

    /**
     * S3 私有密钥
     */
    private String secretKey;

    /**
     * S3 区域，默认 {@code us-east-1}（MinIO / Ceph 基本不看，SDK 仍要求有值）
     */
    private String region = "us-east-1";

    /**
     * 存储桶名称
     */
    private String bucket = "ext-library";

    /**
     * 是否在启动时自动创建存储桶
     */
    private boolean autoCreateBucket = true;

    /**
     * 分片上传的分片大小，默认 10MB，小于 5MB 时按 5MB 处理
     */
    private long partSize = 10485760L;

    /**
     * 预签名链接有效期
     */
    private Duration presignExpiry = Duration.ofHours(1);

    /**
     * 是否使用 path-style 访问，默认开启——MinIO、Ceph、R2 这类自建端点几乎都要求 path-style，
     * 对接 AWS 官方端点时关掉
     */
    private boolean pathStyleAccess = true;

    /**
     * 是否启用 aws-chunked 传输，个别旧网关不认时要关掉
     */
    private boolean chunkedEncoding = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public StorageType getType() {
        return type;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isAutoCreateBucket() {
        return autoCreateBucket;
    }

    public void setAutoCreateBucket(boolean autoCreateBucket) {
        this.autoCreateBucket = autoCreateBucket;
    }

    public long getPartSize() {
        return partSize;
    }

    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    public Duration getPresignExpiry() {
        return presignExpiry;
    }

    public void setPresignExpiry(Duration presignExpiry) {
        this.presignExpiry = presignExpiry;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }

    public boolean isChunkedEncoding() {
        return chunkedEncoding;
    }

    public void setChunkedEncoding(boolean chunkedEncoding) {
        this.chunkedEncoding = chunkedEncoding;
    }

}
