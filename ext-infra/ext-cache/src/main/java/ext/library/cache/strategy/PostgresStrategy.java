package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.postgres.cache.PostgresCacheManager;
import ext.library.postgres.properties.PostgresProperties;

import java.time.Duration;

/**
 * PostgreSQL 缓存策略
 * <p>
 * 使用 PostgreSQL 作为缓存存储，通过 ext-postgres 模块的 {@link PostgresCacheManager} 实现。
 * 需要引入 ext-postgres 模块依赖并配置 PostgreSQL 数据源。
 *
 * @since 4.0.0
 */
public class PostgresStrategy implements CacheStrategy {

    private volatile PostgresCacheManager cacheManager;
    private volatile PostgresProperties postgresProperties;

    /**
     * 获取 PostgresCacheManager（延迟初始化）
     */
    private PostgresCacheManager getCacheManager() {
        if (cacheManager == null) {
            synchronized (this) {
                if (cacheManager == null) {
                    try {
                        cacheManager = SpringUtil.getBean(PostgresCacheManager.class);
                        postgresProperties = SpringUtil.getBean(PostgresProperties.class);
                    } catch (Exception e) {
                        throw new IllegalStateException("""
                                PostgreSQL 缓存不可用，请确保：
                                1. 已引入 ext-postgres 模块依赖
                                2. 已配置 PostgreSQL 数据源""", e);
                    }
                }
            }
        }
        return cacheManager;
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        return getCacheManager().get(genKey(cacheName, key), clazz);
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        getCacheManager().set(genKey(cacheName, key), value, expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public void evict(String cacheName, String key) {
        getCacheManager().delete(genKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
        String pattern = properties.getKeyPrefix() + ":" + cacheName + ":*";
        getCacheManager().deleteByPattern(pattern);
    }

    @Override
    public Duration getDefaultExpireTime() {
        // 优先使用 ext.cache 的配置，否则使用 ext.postgres 的配置
        try {
            CacheProperties cacheProperties = SpringUtil.getBean(CacheProperties.class);
            return cacheProperties.getExpireTime();
        } catch (Exception e) {
            return postgresProperties != null ? postgresProperties.getDefaultCacheExpireTime() : Duration.ofHours(1);
        }
    }
}
