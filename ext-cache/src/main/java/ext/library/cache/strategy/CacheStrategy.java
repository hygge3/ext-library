package ext.library.cache.strategy;

import ext.library.cache.config.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.StringUtil;

import java.time.Duration;

/**
 * 缓存策略
 *
 * @since 2025.08.29
 */
public interface CacheStrategy {

    /**
     * 获取缓存值
     *
     * @param key   缓存 key
     * @param clazz 类型
     *
     * @return {@link T }
     */
    <T> T get(String cacheName, String key, Class<T> clazz);

    /**
     * 保存缓存值
     *
     * @param key        缓存 key
     * @param value      缓存值
     * @param expireTime 过期时间
     *
     * @return {@link T }
     */
    <T> T put(String cacheName, String key, T value, Duration expireTime);

    /**
     * 保存缓存值
     *
     * @param key   缓存 key
     * @param value 缓存值
     */
    <T> T put(String cacheName, String key, T value);

    /**
     * 删除缓存值
     *
     * @param key 缓存 key
     */
    void evict(String cacheName, String key);

    /**
     * 清理所有缓存
     *
     */
    void clear(String cacheName);

    /**
     * 生成 key
     *
     * @param key key
     *
     * @return {@code String }
     */
    default String genKey(String cacheName, String key) {
        return StringUtil.join(Symbol.COLON, SpringUtil.getBean(CacheProperties.class).getKeyPrefix(), cacheName, key);
    }

    /**
     * 获取默认过期时间
     *
     * @return {@link Duration }
     */
    default Duration getDefaultExpireTime() {
        return SpringUtil.getBean(CacheProperties.class).getExpireTime();
    }
}