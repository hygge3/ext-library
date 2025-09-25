package ext.library.ratelimiter.config.properties;

import ext.library.ratelimiter.handler.IRateLimitHandler;
import ext.library.ratelimiter.handler.RateLimiterHandler;
import ext.library.ratelimiter.handler.RedisRateLimitHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 */
@ConfigurationProperties(prefix = RateLimiterProperties.PREFIX)
public class RateLimiterProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "ext.limiter";

    /** 键前缀 */
    private String keyPrefix = "ext.rate_limit";

    /** 限速器类型 */
    private RateLimiterType rateLimiterType;

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public RateLimiterType getRateLimiterType() {
        return rateLimiterType;
    }

    public void setRateLimiterType(RateLimiterType rateLimiterType) {
        this.rateLimiterType = rateLimiterType;
    }

    /**
     * 限速器类型
     *
     * @since 2025.08.29
     */
    public enum RateLimiterType {
        REDIS(new RedisRateLimitHandler()), GUAVA(new RateLimiterHandler());
        private final IRateLimitHandler iRateLimitHandler;

        RateLimiterType(IRateLimitHandler iRateLimitHandler) {
            this.iRateLimitHandler = iRateLimitHandler;
        }

        public IRateLimitHandler getiRateLimitHandler() {
            return iRateLimitHandler;
        }
    }
}