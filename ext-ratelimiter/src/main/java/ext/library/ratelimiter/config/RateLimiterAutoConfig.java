package ext.library.ratelimiter.config;

import ext.library.ratelimiter.aspect.RateLimiterAspect;
import ext.library.ratelimiter.config.properties.RateLimiterProperties;
import ext.library.ratelimiter.handler.IRateLimitHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 速率限制配置
 */
@AutoConfiguration
@EnableConfigurationProperties({RateLimiterProperties.class})
public class RateLimiterAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Bean
    @ConditionalOnMissingBean
    public IRateLimitHandler iRateLimitHandler(RateLimiterProperties rateLimiterProperties) {
        return rateLimiterProperties.getRateLimiterType().getiRateLimitHandler();
    }

    @Bean
    public RateLimiterAspect rateLimitAspect(IRateLimitHandler iRateLimitHandler) {
        log.info("[🚥] 限流器模块载入成功");
        return new RateLimiterAspect(iRateLimitHandler);
    }

}