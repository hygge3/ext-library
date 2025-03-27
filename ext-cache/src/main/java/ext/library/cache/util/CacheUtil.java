package ext.library.cache.util;

import com.github.benmanes.caffeine.cache.Cache;
import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.tool.$;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 缓存操作工具类
 */
@UtilityClass
@Slf4j
public class CacheUtil {

    static final Cache<String, Object> CACHE = SpringUtil.getBean(Cache.class);
    static final CacheProperties CACHE_PROPERTIES = SpringUtil.getBean(CacheProperties.class);

    /**
     * 获取缓存值
     *
     * @param key        缓存 key
     */
    public <T> T get(String key, Class<T> clazz) {
        String cachekey = genKey(key);
        // 读写，查询 Caffeine
        Object caffeineCache = CACHE.getIfPresent(cachekey);

        if ($.isNotNull(caffeineCache)) {
            log.debug("get data from caffeine");
            return clazz.cast(caffeineCache);
        }

        // 查询 Redis
        T redisCache = RedisUtil.get(cachekey, clazz);
        if ($.isNotNull(redisCache)) {
            log.debug("get data from redis");
            CACHE.put(cachekey, redisCache);
            return redisCache;
        }
        return null;
    }

    /**
     * 获取缓存值
     *
     * @param key        缓存 key
     */
    public Object get(String key) {
        String cachekey = genKey(key);
        // 读写，查询 Caffeine
        Object caffeineCache = CACHE.getIfPresent(cachekey);

        if ($.isNotNull(caffeineCache)) {
            log.debug("get data from caffeine");
            return caffeineCache;
        }

        // 查询 Redis
        Object redisCache = RedisUtil.get(cachekey);
        if ($.isNotNull(redisCache)) {
            log.debug("get data from redis");
            CACHE.put(cachekey, redisCache);
            return redisCache;
        }
        return null;
    }

    /**
     * 保存缓存值
     *
     * @param key        缓存 key
     * @param value      缓存值
     */
    public <T> T put(String key, T value) {
        return put(key, value, CACHE_PROPERTIES.getExpireTime(), TimeUnit.SECONDS);
    }

    /**
     * 保存缓存值
     *
     * @param key        缓存 key
     * @param value      缓存值
     */
    public <T> T put(String key, T value, long timeout, TimeUnit timeUnit) {
        String cachekey = genKey(key);
        RedisUtil.set(cachekey, JsonUtil.toJson(value), timeout, timeUnit);
        CACHE.put(cachekey, value);
        return value;
    }

    /**
     * 删除缓存值
     *
     * @param key        缓存 key
     */
    public void evict(String key) {
        String cachekey = genKey(key);
        RedisUtil.del(cachekey);
        CACHE.invalidate(cachekey);
    }

    /**
     * 生成 key
     *
     * @param key key
     * @return {@code String }
     */
    public String genKey(String key) {
        return CACHE_PROPERTIES.getKeyPrefix() + CACHE_PROPERTIES.getDelimiter() + key;
    }
}
