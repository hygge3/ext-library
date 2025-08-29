package ext.library.ratelimiter.config.properties;

import ext.library.ratelimiter.enums.RateLimiterType;
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

    /**
     * 开启限流
     */
    private boolean enable = true;

    private RateLimiterType rateLimiterType;

}