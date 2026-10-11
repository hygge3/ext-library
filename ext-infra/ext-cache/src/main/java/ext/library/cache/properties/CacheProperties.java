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
     * 缓存存储方式，默认为自动检测
     * <p>
     * {@link CacheStorage#AUTO} 时根据类路径中存在的依赖自动选择：有 ext-redis 或 ext-postgres 则使用 L2，否则使用 CAFFEINE。
     */
    private CacheStorage cacheStorage = CacheStorage.AUTO;

    /**
     * 二级缓存后端类型，默认为自动检测
     * <p>
     * 当 cacheStorage 为 {@link CacheStorage#L2} 或 {@link CacheStorage#AUTO} 时，指定第二级（分布式）缓存的后端。
     * <ul>
     *     <li>{@link L2Backend#AUTO} - 自动检测，优先 Redis，其次 PostgreSQL（默认）</li>
     *     <li>{@link L2Backend#REDIS} - 使用 Redis（需要 ext-redis 模块）</li>
     *     <li>{@link L2Backend#POSTGRES} - 使用 PostgreSQL（需要 ext-postgres 模块）</li>
     * </ul>
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
