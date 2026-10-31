package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import ext.library.tool.util.StringUtil;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Type;
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
     * @param key  缓存 key
     * @param type 类型
     *
     * @return {@link T }
     */
    <T> T get(String cacheName, String key, Type type);

    /**
     * 内存缓存值的类型窄化
     * <p>
     * 基本类型的 {@code Class#cast} 不接受包装类（{@code int.class.cast(1)} 直接抛异常），所以先装箱再判类型；
     * 类型对不上说明缓存里是旧格式或其他方法写的值，按未命中处理，不让 {@code ClassCastException} 冒到调用方。
     *
     * @param value 缓存中的原始值
     * @param type  期望类型
     *
     * @return 窄化后的值，类型不符或值为 {@code null} 时返回 {@code null}
     */
    @SuppressWarnings("unchecked")
    default <T> T narrow(Object value, Type type) {
        if (value == null) {
            return null;
        }
        if (type instanceof Class<?> clazz) {
            Class<?> target = clazz.isPrimitive() ? ClassUtils.resolvePrimitiveIfNecessary(clazz) : clazz;
            return target.isInstance(value) ? (T) value : null;
        }
        // 泛型类型擦除后无法判断，本地缓存存的就是同一份对象引用，原样返回
        return (T) value;
    }

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
     * 缓存剩余有效期
     * <p>
     * L2 回填本地缓存时必须沿用分布式层的剩余时间，否则本地副本会活得比分布式层久，
     * 分布式层已经过期了本地还在供应旧值。
     *
     * @param cacheName 缓存名称
     * @param key       缓存 key
     *
     * @return 剩余有效期；{@code null} 表示未知或永不过期
     */
    default Duration remainingTtl(String cacheName, String key) {
        return null;
    }

    /**
     * 只管分布式层的视图
     * <p>
     * L2 的本地层只是读缓存，删除/修改只影响本机，其他实例的本地副本会继续供旧值。
     * 需要「写完全网可见」的数据（会话、token、封禁状态）必须走这个视图。
     *
     * @return 非 L2 实现返回自身
     */
    default CacheStrategy distributed() {
        return this;
    }

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
