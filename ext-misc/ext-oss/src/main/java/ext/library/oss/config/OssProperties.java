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
     * 本地存储对外访问前缀
     */
    private String urlPrefix = "";

    /**
     * S3 服务地址，如 {@code https://s3.example.com}
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
     * S3 区域
     */
    private String region;

    /**
     * 存储桶名称
     */
    private String bucket = "ext-library";

    /**
     * 是否在启动时自动创建存储桶
     */
    private boolean autoCreateBucket = true;

    /**
     * 分片上传的分片大小，默认 10MB
     */
    private long partSize = 10485760L;

    /**
     * 预签名链接有效期
     */
    private Duration presignExpiry = Duration.ofHours(1);

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

}
