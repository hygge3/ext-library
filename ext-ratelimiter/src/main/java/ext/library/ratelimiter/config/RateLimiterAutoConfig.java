package ext.library.ratelimiter.config;

import ext.library.ratelimiter.aspect.RateLimiterAspect;
import ext.library.ratelimiter.config.properties.RateLimiterProperties;
import ext.library.ratelimiter.handler.IRateLimitHandler;
import ext.library.ratelimiter.handler.RateLimiterHandler;
import ext.library.ratelimiter.handler.RedisRateLimitHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 速率限制配置
 */
@AutoConfiguration
@EnableConfigurationProperties({RateLimiterProperties.class})
public class RateLimiterAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "enable", havingValue = "true")
    public RateLimiterAspect rateLimitAspect(RateLimiterProperties rateLimiterProperties) {
        IRateLimitHandler rateLimitHandler = switch (rateLimiterProperties.getRateLimiterType()) {
            case GUAVA -> new RateLimiterHandler();
            case REDIS -> new RedisRateLimitHandler();
        };
        return new RateLimiterAspect(rateLimitHandler);
    }

}