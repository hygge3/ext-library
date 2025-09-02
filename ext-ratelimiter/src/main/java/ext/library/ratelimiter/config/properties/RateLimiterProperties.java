package ext.library.ratelimiter.config.properties;

import ext.library.ratelimiter.handler.IRateLimitHandler;
import ext.library.ratelimiter.handler.RateLimiterHandler;
import ext.library.ratelimiter.handler.RedisRateLimitHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 */
@Getter
@Setter
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

    /**
     * 限速器类型
     *
     * @since 2025.08.29
     */
    @Getter
    @AllArgsConstructor
    public enum RateLimiterType {
        REDIS(new RedisRateLimitHandler()), GUAVA(new RateLimiterHandler());
        private final IRateLimitHandler iRateLimitHandler;
    }
}