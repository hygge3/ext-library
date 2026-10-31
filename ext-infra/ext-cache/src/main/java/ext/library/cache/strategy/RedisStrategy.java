package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * Redis 分布式缓存策略
 * <p>
 * 使用 Redis 作为缓存存储，支持分布式环境下的缓存共享。
 *
 * @since 2025.08.29
 */
public class RedisStrategy implements CacheStrategy {

    @Override
    public <T> T get(String cacheName, String key, Type type) {
        return RedisUtil.get(genKey(cacheName, key), type);
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        RedisUtil.set(genKey(cacheName, key), JsonUtil.toJson(value), expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime) {
        return RedisUtil.setNxEx(genKey(cacheName, key), JsonUtil.toJson(value), expireTime);
    }

    @Override
    public void evict(String cacheName, String key) {
        RedisUtil.del(genKey(cacheName, key));
    }

    @Override
    public Duration remainingTtl(String cacheName, String key) {
        // Redis 负值表示 key 不存在 / 永不过期，两者都不给出具体剩余时间
        Long ttl = RedisUtil.ttl(genKey(cacheName, key));
        return ttl == null || ttl <= 0 ? null : Duration.ofSeconds(ttl);
    }

    @Override
    public void clear(String cacheName) {
        CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
        String pattern = properties.getKeyPrefix() + ":" + cacheName + ":*";
        RedisUtil.patternUnlink(pattern);
    }
}