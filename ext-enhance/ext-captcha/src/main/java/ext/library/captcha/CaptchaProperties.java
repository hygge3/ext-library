package ext.library.captcha;

import ext.library.cache.enums.CacheStorage;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

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

    /**
     * 缓存存储方式
     */
    private CacheStorage cacheStorage = CacheStorage.CAFFEINE;

    /**
     * 验证码 cache 名，默认：captcha
     */
    private String cacheName = "captcha";

    /**
     * 缓存过期时间，默认 5 分钟
     * <p>支持格式：{@code 5m}、{@code 300s}、{@code PT5M}
     */
    private Duration expireTime = Duration.ofMinutes(5);

    // === 图像配置 ===

    /**
     * 验证码图像宽度，默认 130
     */
    private int width = 130;

    /**
     * 验证码图像高度，默认 48
     */
    private int height = 48;

    /**
     * 验证码字符长度，默认 4
     */
    private int codeLength = 4;

    /**
     * 图像格式，默认 JPEG
     */
    private String imageFormat = "JPEG";

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

    public Duration getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Duration expireTime) {
        this.expireTime = expireTime;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = codeLength;
    }

    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }
}
