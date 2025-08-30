package ext.library.cache.config.properties;

import ext.library.cache.enums.CacheStorage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存属性
 */
@Getter
@Setter
@ConfigurationProperties(prefix = CacheProperties.PREFIX)
public class CacheProperties {

    public static final String PREFIX = "ext.cache";

    /**
     * 通用的 key 前缀
     */
    private String keyPrefix = "ext:cache";

    /**
     * 空值标识
     */
    private String nullValue = "N_V";

    /**
     * 默认缓存数据的超时时间 (s)
     */
    private Duration expireTime = Duration.ofSeconds(86400L);

    /** 缓存存储方式 */
    private CacheStorage cacheStorage = CacheStorage.L2;
}