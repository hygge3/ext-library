package ext.library.cache.properties;

import ext.library.cache.enums.CacheStorage;
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
     * 过期时间抖动比例：{@code 0} 关闭，{@code 0.1} 表示在 {@code [expire × 0.9, expire]} 内随机
     * <p>
     * 同一时刻首次访问的一批 key，TTL 完全相同就会在同一秒集体过期，回源流量瞬间砸到数据库。
     * 只缩短不延长：抖动后仍不超过调用方给的上限。
     */
    private double expireJitter = 0.1;

    /**
     * 单飞时等待同 key 首次加载的上限，超时后自行回源
     */
    private Duration loadTimeout = Duration.ofSeconds(3);

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

    public double getExpireJitter() {
        return expireJitter;
    }

    public void setExpireJitter(double expireJitter) {
        this.expireJitter = expireJitter;
    }

    /**
     * 单飞等待上限
     *
     * @return 超时时长
     */
    public Duration getLoadTimeout() {
        return loadTimeout;
    }

    /**
     * 设置单飞等待上限
     *
     * @param loadTimeout 超时时长
     */
    public void setLoadTimeout(Duration loadTimeout) {
        this.loadTimeout = loadTimeout;
    }

    public void setCacheStorage(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
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
