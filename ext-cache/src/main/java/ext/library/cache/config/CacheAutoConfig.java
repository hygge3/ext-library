package ext.library.cache.config;

import ext.library.cache.config.properties.CacheProperties;
import ext.library.cache.strategy.CacheStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存自动配置
 *
 * @since 2025.08.29
 */
@AutoConfiguration
@EnableConfigurationProperties({CacheProperties.class})
public class CacheAutoConfig {
    @Bean
    public CacheStrategy rateLimitAspect(CacheProperties cacheProperties) {
        return cacheProperties.getCacheStorage().getCacheStrategy();

    }
}