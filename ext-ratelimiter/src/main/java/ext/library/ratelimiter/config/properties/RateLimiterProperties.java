package ext.library.ratelimiter.config.properties;

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

    /** 限速器类型 */
    private RateLimiterType rateLimiterType;

    /**
     * 限速器类型
     *
     * @since 2025.08.29
     */
    public enum RateLimiterType {
        REDIS, GUAVA
    }
}