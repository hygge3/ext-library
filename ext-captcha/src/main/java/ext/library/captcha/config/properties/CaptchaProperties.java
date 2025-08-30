package ext.library.captcha.config.properties;

import ext.library.cache.enums.CacheStorage;
import ext.library.captcha.enums.CaptchaType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置
 */
@Getter
@Setter
@ConfigurationProperties(CaptchaProperties.PREFIX)
public class CaptchaProperties {

    public static final String PREFIX = "ext.captcha";

    /**
     * 验证码类型，默认：随机数
     */
    CaptchaType captchaType = CaptchaType.RANDOM;

    /** 缓存存储方式 */
    CacheStorage cacheStorage = CacheStorage.CAFFEINE;

    /**
     * 验证码 cache 名，默认：captcha
     */
    String cacheName = "captcha";

    /**
     * 默认缓存的超时时间 (s),默认 5min
     */
    int expireTime = 300;
}