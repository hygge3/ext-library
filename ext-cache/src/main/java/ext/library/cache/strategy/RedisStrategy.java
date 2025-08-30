package ext.library.cache.strategy;

import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;

import java.time.Duration;

/**
 * Redis 策略
 *
 * @since 2025.08.29
 */
public class RedisStrategy implements CacheStrategy {
    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        return RedisUtil.get(genKey(cacheName, key), clazz);
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
    public void evict(String cacheName, String key) {
        RedisUtil.del(genKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        RedisUtil.patternUnlink(cacheName + ":*");
    }
}