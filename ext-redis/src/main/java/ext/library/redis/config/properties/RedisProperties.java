package ext.library.redis.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存属性
 */
@ConfigurationProperties(prefix = RedisProperties.PREFIX)
public class RedisProperties {

    static final String PREFIX = "ext.redis";

    /**
     * 通用的 key 前缀
     */
    private String keyPrefix;

    /**
     * 默认锁的超时时间
     */
    private Duration defaultLockTimeout = Duration.ofSeconds(10L);


    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getDefaultLockTimeout() {
        return defaultLockTimeout;
    }

    public void setDefaultLockTimeout(Duration defaultLockTimeout) {
        this.defaultLockTimeout = defaultLockTimeout;
    }
}