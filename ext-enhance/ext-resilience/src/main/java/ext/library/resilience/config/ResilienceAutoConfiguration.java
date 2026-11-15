package ext.library.resilience.config;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.metrics.MetricsRecorder;
import ext.library.resilience.degrade.DegradeAspect;
import ext.library.resilience.degrade.DegradeRegistry;
import ext.library.resilience.ratelimit.RateLimitAspect;
import ext.library.resilience.ratelimit.RateLimitKeyGenerator;
import ext.library.resilience.retry.RetryAspect;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 限流与降级模块自动配置
 * <p>
 * 限流的计数后端跟随 {@code ext.cache.storage}，本模块只要求容器里有 {@link CacheStrategy}；
 * 降级与重试都是纯进程内实现，没有缓存也能用。
 */
@AutoConfiguration
@EnableConfigurationProperties(ResilienceProperties.class)
public class ResilienceAutoConfiguration {

    /**
     * 默认的限流 Key 生成器
     *
     * @return Key 生成器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public RateLimitKeyGenerator rateLimitKeyGenerator() {
        return new RateLimitKeyGenerator.Default();
    }

    /**
     * 限流切面
     *
     * @param cacheStrategy 缓存策略，令牌桶落在这里
     * @param keyGenerator  Key 生成器
     * @param properties    配置，取自告警阈值
     * @return 限流切面实例
     */
    @Bean
    @ConditionalOnBean(CacheStrategy.class)
    public RateLimitAspect rateLimitAspect(CacheStrategy cacheStrategy, RateLimitKeyGenerator keyGenerator,
                                           ResilienceProperties properties, ObjectProvider<MetricsRecorder> metrics) {
        Logs.info(EmojiSymbol.RATELIMIT, "载入模块：限流（后端: {}）", cacheStrategy.backendType());
        return new RateLimitAspect(cacheStrategy, keyGenerator, properties.getRateLimit().getWarnThreshold(),
                metrics.getIfAvailable());
    }

    /**
     * 熔断器注册表
     *
     * @return 注册表实例
     */
    @Bean
    @ConditionalOnMissingBean
    public DegradeRegistry degradeRegistry() {
        return new DegradeRegistry();
    }

    /**
     * 降级切面
     *
     * @param registry 熔断器注册表
     * @return 降级切面实例
     */
    @Bean
    public DegradeAspect degradeAspect(DegradeRegistry registry) {
        Logs.info(EmojiSymbol.DEGRADE, "载入模块：降级");
        return new DegradeAspect(registry);
    }

    /**
     * 重试切面
     *
     * @return 重试切面实例
     */
    @Bean
    @ConditionalOnMissingBean
    public RetryAspect retryAspect() {
        Logs.info(EmojiSymbol.RETRY, "载入模块：重试");
        return new RetryAspect();
    }

}
