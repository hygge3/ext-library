package ext.library.captcha.config.properties;

import ext.library.cache.enums.CacheStorage;
import ext.library.captcha.enums.CaptchaType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置
 */
@ConfigurationProperties(CaptchaProperties.PREFIX)
public class CaptchaProperties {

    public static final String PREFIX = "ext.captcha";

    /**
     * 验证码类型，默认：随机数
     */
    private CaptchaType captchaType = CaptchaType.RANDOM;

    /** 缓存存储方式 */
    private CacheStorage cacheStorage = CacheStorage.CAFFEINE;

    /**
     * 验证码 cache 名，默认：captcha
     */
    private String cacheName = "captcha";

    /**
     * 默认缓存的超时时间 (s),默认 5min
     */
    private int expireTime = 300;

    public CaptchaType getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(CaptchaType captchaType) {
        this.captchaType = captchaType;
    }

    public CacheStorage getCacheStorage() {
        return cacheStorage;
    }

    public void setCacheStorage(CacheStorage cacheStorage) {
        this.cacheStorage = cacheStorage;
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }
}