package ext.library.captcha.cache;

import ext.library.captcha.core.CaptchaUtil;
import ext.library.tool.constant.Symbol;

/**
 * 验证码缓存
 *
 * <p>
 * 1.单服务可以采用 guava、ehcache、caffeine 等内存缓存。2.分布式下可以使用 redis 等。
 * </p>
 */
public interface ICaptchaCache {

    /**
     * 保存缓存
     *
     * <p>
     * 非 spring cache 等启动就确定超时的缓存，重新改方法
     * </p>
     *
     * @param cacheKey    缓存 key
     * @param value       缓存 value
     * @param ttlInMillis ttl
     */
    default void put(String cacheKey, String value, long ttlInMillis) {

    }

    /**
     * 保存缓存
     *
     * @param cacheName 缓存空间
     * @param uuid      验证码 uuid
     * @param value     缓存 value
     */
    default void put(String cacheName, String uuid, String value) {
        long ttlInMillis = CaptchaUtil.getTTLFormCacheName(cacheName);
        String cacheKey = cacheName + Symbol.COLON + uuid;
        put(cacheKey, value, ttlInMillis);
    }

    /**
     * 获取并删除缓存，验证码不管成功只能验证一次
     *
     * <p>
     * 非 spring cache 等启动就确定超时的缓存，重新改方法
     * </p>
     *
     * @param cacheKey 缓存空间
     * @return 验证码
     */
    default String getAndRemove(String cacheKey) {
        return null;
    }

    /**
     * 获取并删除缓存，验证码不管成功只能验证一次
     *
     * @param cacheName 缓存空间
     * @param uuid      验证码 uuid
     * @return 验证码
     */
    default String getAndRemove(String cacheName, String uuid) {
        String cacheKey = cacheName + Symbol.COLON + uuid;
        return getAndRemove(cacheKey);
    }

}
