package ext.library.captcha.cache;

import ext.library.cache.util.CacheUtil;
import ext.library.captcha.config.properties.CaptchaProperties;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * spring cache 的 captcha cache
 */
@RequiredArgsConstructor
public class SpringCacheCaptchaCache implements ICaptchaCache {

    final CaptchaProperties properties;

    @Override
    public void put(String cacheName, String uuid, String value) {
        CacheUtil.put(cacheName + uuid, value, properties.getExpireTime(), TimeUnit.SECONDS);
    }

    @Override
    public String getAndRemove(String cacheName, String uuid) {
        String value = CacheUtil.get(cacheName + uuid, String.class);
        if (value != null) {
            CacheUtil.evict(cacheName + uuid);
        }
        return value;
    }

}