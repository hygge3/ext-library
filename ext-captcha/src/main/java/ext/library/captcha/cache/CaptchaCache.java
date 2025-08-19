package ext.library.captcha.cache;

import ext.library.cache.util.CacheUtil;
import ext.library.captcha.config.properties.CaptchaProperties;
import ext.library.tool.constant.Symbol;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 验证码缓存
 */
@RequiredArgsConstructor
public class CaptchaCache {

    final CaptchaProperties properties;

    /**
     * 保存缓存
     *
     * @param cacheName 缓存空间
     * @param uuid      验证码 uuid
     * @param value     缓存 value
     */
    public void put(String cacheName, String uuid, String value) {
        String cacheKey = cacheName + Symbol.COLON + uuid;
        CacheUtil.put(cacheKey, value, properties.getExpireTime(), TimeUnit.SECONDS);

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
        String cacheKey = cacheName + Symbol.COLON + uuid;
        String value = CacheUtil.get(cacheKey, String.class);
        if (Objects.isNull(value)) {
            return null;
        }
        CacheUtil.evict(cacheKey);
        return value;
    }

}