package ext.library.cache.properties;

import ext.library.cache.enums.CacheStorage;
import ext.library.cache.enums.L2Backend;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 缓存配置属性
 * <p>
 * 配置前缀：{@code ext.cache}
 *
 * @since 2025.08.29
 */
@ConfigurationProperties(prefix = CacheProperties.PREFIX)
public class CacheProperties {

    public static final String PREFIX = "ext.cache";

    /**
     * 通用的 key 前缀
     */
    private String keyPrefix = "ext:cache";

    /**
     * 默认缓存数据的超时时间
     */
    private Duration expireTime = Duration.ofSeconds(86400L);

    /**
     * 缓存存储方式，默认为自动检测。AUTO 时根据类路径中存在的依赖自动选择：有 ext-redis 或 ext-postgres 则使用 L2，否则使用 CAFFEINE
     */
    private CacheStorage cacheStorage = CacheStorage.AUTO;

    /**
     * 二级缓存后端类型，默认为自动检测。当 cacheStorage 为 L2 或 AUTO 时生效。AUTO 优先 Redis，其次 PostgreSQL
     */
    private L2Backend l2Backend = L2Backend.AUTO;

    /**
     * Caffeine 缓存配置
     */
    private CaffeineConfig caffeine = new CaffeineConfig();

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Duration expireTime) {
        this.expireTime = expireTime;
    }

    public CacheStorage getCacheStorage() {
        return cacheStorage;
    }

    public void setCacheStorage(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    public L2Backend getL2Backend() {
        return l2Backend;
    }

    public void setL2Backend(L2Backend l2Backend) {
        this.l2Backend = l2Backend;
    }

    public CaffeineConfig getCaffeine() {
        return caffeine;
    }

    public void setCaffeine(CaffeineConfig caffeine) {
        this.caffeine = caffeine;
    }

    /**
     * Caffeine 缓存配置
     */
    public static class CaffeineConfig {
        /**
         * 最大缓存条目数
         */
        private long maximumSize = 10000L;

        /**
         * 访问后是否刷新过期时间
         */
        private boolean refreshOnAccess = true;

        public long getMaximumSize() {
            return maximumSize;
        }

        public void setMaximumSize(long maximumSize) {
            this.maximumSize = maximumSize;
        }

        public boolean isRefreshOnAccess() {
            return refreshOnAccess;
        }

        public void setRefreshOnAccess(boolean refreshOnAccess) {
            this.refreshOnAccess = refreshOnAccess;
        }
    }
}
