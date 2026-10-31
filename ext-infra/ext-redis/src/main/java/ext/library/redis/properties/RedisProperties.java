package ext.library.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存属性
 */
@ConfigurationProperties(prefix = RedisProperties.PREFIX)
public class RedisProperties {

    public static final String PREFIX = "ext.redis";

    /**
     * 通用的 key 前缀
     */
    private String keyPrefix;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }
}
