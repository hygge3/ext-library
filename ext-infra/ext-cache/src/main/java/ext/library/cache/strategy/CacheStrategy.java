package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
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
     * 当 key 不存在时写入缓存值（后端原子操作）
     * <p>
     * 实现必须是后端本身的原子操作（Caffeine {@code asMap().putIfAbsent}、Redis {@code SET NX PX}、
     * PostgreSQL {@code INSERT … ON CONFLICT DO NOTHING}），不能用 {@code get} + {@code put} 拼出来，
     * 否则并发场景下两边会同时判定“不存在”而双双写入。
     *
     * @param cacheName  缓存名称
     * @param key        缓存 key
     * @param value      缓存值
     * @param expireTime 过期时间
     *
     * @return {@code true} 写入成功（key 原本不存在）；{@code false} key 已存在
     */
    boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime);

    /**
     * 生成 key
     *
     * @param key key
     *
     * @return {@code String }
     */
    default String genKey(String cacheName, String key) {
        return StringUtil.join(":", SpringUtil.getBean(CacheProperties.class).getKeyPrefix(), cacheName, key);
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
