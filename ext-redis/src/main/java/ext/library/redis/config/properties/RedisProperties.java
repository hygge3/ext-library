package ext.library.redis.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存属性
 */
@Getter
@Setter
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


}