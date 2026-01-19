package ext.library.cache.strategy;

import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;

import java.time.Duration;
import java.util.Objects;

/**
 * 二级缓存 策略
 *
 * @since 2025.08.29
 */
public class L2Strategy implements CacheStrategy {
    final CacheStrategy redisStrategy = new RedisStrategy();
    final CacheStrategy caffeineStrategy = new CaffeineStrategy();

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        // 读写，查询 Caffeine
        T caffeineCache = caffeineStrategy.get(cacheName, key, clazz);
        if (Objects.nonNull(caffeineCache)) {
            Logs.debug(EmojiSymbol.CACHE,"从 Caffeine 中获取数据");
            return clazz.cast(caffeineCache);
        }

        // 查询 Redis
        T redisCache = redisStrategy.get(cacheName, key, clazz);
        if (Objects.nonNull(redisCache)) {
            Logs.debug(EmojiSymbol.CACHE,"从 Redis 获取数据");
            redisStrategy.put(cacheName, key, redisCache);
            return redisCache;
        }
        return null;
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        redisStrategy.put(cacheName, key, JsonUtil.toJson(value), expireTime);
        caffeineStrategy.put(cacheName, key, value, expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        redisStrategy.put(cacheName, key, JsonUtil.toJson(value));
        caffeineStrategy.put(cacheName, key, value);
        return value;
    }

    @Override
    public void evict(String cacheName, String key) {
        redisStrategy.evict(cacheName, key);
        caffeineStrategy.evict(cacheName, key);
    }

    @Override
    public void clear(String cacheName) {
        redisStrategy.clear(cacheName);
        caffeineStrategy.clear(cacheName);
    }
}
