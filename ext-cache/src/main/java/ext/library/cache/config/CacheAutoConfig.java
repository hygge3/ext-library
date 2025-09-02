package ext.library.cache.config;

import ext.library.cache.config.properties.CacheProperties;
import ext.library.cache.core.CacheAspect;
import ext.library.cache.strategy.CacheStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存自动配置
 *
 * @since 2025.08.29
 */

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({CacheProperties.class})
public class CacheAutoConfig {

    @Bean
    @ConditionalOnMissingBean
    public CacheStrategy cacheStrategy(CacheProperties cacheProperties) {
        return cacheProperties.getCacheStorage().getCacheStrategy();
    }

    @Bean
    public CacheAspect cacheAspect(CacheStrategy cacheStrategy) {
        log.info("[💾] 缓存模块载入成功");
        return new CacheAspect(cacheStrategy);
    }
}