package ext.library.ratelimiter.config;

import ext.library.ratelimiter.aspect.RateLimiterAspect;
import ext.library.ratelimiter.config.properties.LimiterProperties;
import ext.library.ratelimiter.handler.IRateLimitHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 速率限制配置
 */
@Configuration
@EnableConfigurationProperties({LimiterProperties.class})
public class LimiterAutoConfig {

    @Bean
    @ConditionalOnProperty(prefix = LimiterProperties.PREFIX, name = "enable", havingValue = "true")
    public RateLimiterAspect rateLimitAspect(IRateLimitHandler rateLimitHandler) {
        return new RateLimiterAspect(rateLimitHandler);
    }

}