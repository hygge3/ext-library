package ext.library.captcha.cache;

import ext.library.cache.strategy.CacheStrategy;

import java.util.Objects;

/**
 * 验证码缓存
 */
public class CaptchaCache {

    private final CacheStrategy cacheStrategy;

    public CaptchaCache(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    /**
     * 保存缓存
     *
     * @param cacheName 缓存空间
     * @param uuid      验证码 uuid
     * @param value     缓存 value
     */
    public void put(String cacheName, String uuid, String value) {
        cacheStrategy.put(cacheName, uuid, value);
    }


    /**
     * 获取并删除缓存，验证码不管成功只能验证一次
     *
     * @param cacheName 缓存空间
     * @param uuid      验证码 uuid
     *
     * @return 验证码
     */
    public String getAndRemove(String cacheName, String uuid) {
        String value = cacheStrategy.get(cacheName, uuid, String.class);
        if (Objects.isNull(value)) {
            return null;
        }
        cacheStrategy.evict(cacheName, uuid);
        return value;
    }

}