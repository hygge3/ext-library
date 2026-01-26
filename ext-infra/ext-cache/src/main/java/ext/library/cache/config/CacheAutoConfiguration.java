package ext.library.cache.config;

import ext.library.cache.core.CacheAspect;
import ext.library.cache.properties.CacheProperties;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存自动配置
 *
 * @since 2025.08.29
 */
@AutoConfiguration
@EnableConfigurationProperties({CacheProperties.class})
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheStrategy cacheStrategy(CacheProperties cacheProperties) {
        return cacheProperties.getCacheStorage().getCacheStrategy();
    }

    @Bean
    public CacheAspect cacheAspect(CacheStrategy cacheStrategy) {
        Logs.info(EmojiSymbol.CACHE, "缓存模块载入成功");
        return new CacheAspect(cacheStrategy);
    }
}
