package ext.library.ratelimiter.config;

import ext.library.ratelimiter.aspect.RateLimiterAspect;
import ext.library.ratelimiter.handler.IRateLimitHandler;
import ext.library.ratelimiter.properties.RateLimiterProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
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

    @Bean
    @ConditionalOnMissingBean
    public IRateLimitHandler iRateLimitHandler(RateLimiterProperties rateLimiterProperties) {
        return rateLimiterProperties.getRateLimiterType().getiRateLimitHandler();
    }

    @Bean
    public RateLimiterAspect rateLimitAspect(IRateLimitHandler iRateLimitHandler) {
        Logs.info(EmojiSymbol.RATELIMITER, "限流器模块载入");
        return new RateLimiterAspect(iRateLimitHandler);
    }

}
