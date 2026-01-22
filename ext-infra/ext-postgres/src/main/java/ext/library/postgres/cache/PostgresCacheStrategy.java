package ext.library.postgres.cache;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.postgres.properties.PostgresProperties;

import java.time.Duration;

/**
 * PostgreSQL 缓存策略
 * <p>
 * 实现 {@link CacheStrategy} 接口，可与 ext-cache 模块无缝集成
 *
 * @since 4.0.0
 */
public class PostgresCacheStrategy implements CacheStrategy {

    private final PostgresCacheManager cacheManager;
    private final PostgresProperties properties;

    public PostgresCacheStrategy(PostgresCacheManager cacheManager, PostgresProperties properties) {
        this.cacheManager = cacheManager;
        this.properties = properties;
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        return cacheManager.get(genKey(cacheName, key), clazz);
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        cacheManager.set(genKey(cacheName, key), value, expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public void evict(String cacheName, String key) {
        cacheManager.delete(genKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        cacheManager.deleteByPattern(cacheName + ":*");
    }

    @Override
    public Duration getDefaultExpireTime() {
        return properties.getDefaultCacheExpireTime();
    }
}
