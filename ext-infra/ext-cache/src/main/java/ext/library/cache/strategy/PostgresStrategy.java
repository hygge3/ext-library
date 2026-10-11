package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.postgres.util.PostgresUtil;

import java.time.Duration;

/**
 * PostgreSQL 缓存策略
 * <p>
 * 使用 PostgreSQL 作为缓存存储，通过 ext-postgres 模块的 {@link PostgresUtil} 实现。
 * 需要引入 ext-postgres 模块依赖并配置 PostgreSQL 数据源。
 *
 * @since 4.0.0
 */
public class PostgresStrategy implements CacheStrategy {

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        return PostgresUtil.cacheGet(genKey(cacheName, key), clazz);
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        PostgresUtil.cacheSet(genKey(cacheName, key), value, expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public void evict(String cacheName, String key) {
        PostgresUtil.cacheDelete(genKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
        String pattern = properties.getKeyPrefix() + ":" + cacheName + ":*";
        PostgresUtil.cacheDeleteByPattern(pattern);
    }
}
