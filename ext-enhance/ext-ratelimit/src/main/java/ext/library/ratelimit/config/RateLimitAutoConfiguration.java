package ext.library.ratelimit.config;

import ext.library.postgres.ratelimit.PostgresRateLimiter;
import ext.library.ratelimit.RateLimitAspect;
import ext.library.ratelimit.RateLimitKeyGenerator;
import ext.library.ratelimit.store.MemoryRateLimitStore;
import ext.library.ratelimit.store.PostgresRateLimitStore;
import ext.library.ratelimit.store.RateLimitStore;
import ext.library.ratelimit.store.RedisRateLimitStore;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/**
 * 限流模块自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfiguration {

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
     * @param rateLimitStore 限流存储
     * @param keyGenerator   Key 生成器
     *
     * @return 限流切面实例
     */
    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitStore rateLimitStore, RateLimitKeyGenerator keyGenerator) {
        Logs.info(EmojiSymbol.RATELIMIT, "载入模块：限流（RateLimitStore: {}）", rateLimitStore.getClass().getSimpleName());
        return new RateLimitAspect(rateLimitStore, keyGenerator);
    }

    // ──────────── 显式配置 ────────────

    /**
     * 显式配置：Redis 存储
     * <p>
     * 当 {@code ext.ratelimit.store-type=REDIS} 且类路径中存在 {@code ext-redis} 依赖时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.redis.util.RedisUtil")
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "store-type", havingValue = "REDIS")
    static class RedisRateLimitStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(RateLimitStore.class)
        public RateLimitStore rateLimitStore() {
            return new RedisRateLimitStore();
        }

    }

    /**
     * 显式配置：PostgreSQL 存储
     * <p>
     * 当 {@code ext.ratelimit.store-type=POSTGRES} 且类路径中存在 {@code ext-postgres} 依赖时生效。
     * 未取到 {@link PostgresRateLimiter}（缺少数据源）时退化为内存存储。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.postgres.util.PostgresUtil")
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "store-type", havingValue = "POSTGRES")
    static class PostgresRateLimitStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(RateLimitStore.class)
        public RateLimitStore rateLimitStore(ObjectProvider<PostgresRateLimiter> rateLimiter) {
            PostgresRateLimiter limiter = rateLimiter.getIfAvailable();
            return limiter == null ? new MemoryRateLimitStore() : new PostgresRateLimitStore(limiter);
        }

    }

    /**
     * 显式配置：内存存储
     * <p>
     * 当 {@code ext.ratelimit.store-type=MEMORY} 时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "store-type", havingValue = "MEMORY")
    static class MemoryRateLimitStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(RateLimitStore.class)
        public RateLimitStore rateLimitStore() {
            return new MemoryRateLimitStore();
        }

    }

    // ──────────── 自动检测 ────────────

    /**
     * 自动检测限流存储实现
     * <p>
     * 当 {@code ext.ratelimit.store-type} 未配置或显式设置为 {@code AUTO} 时生效，
     * 根据类路径中可用的依赖自动选择存储实现，优先级：Redis > Memory。
     * <p>
     * PostgreSQL 不参与自动检测，因为它需要 {@code JdbcClient} 才能工作，
     * 仅有 JDBC 驱动并不足以启用；需要它时请显式配置 {@code store-type=POSTGRES}。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = RateLimitProperties.PREFIX,
            name = "store-type",
            havingValue = "AUTO",
            matchIfMissing = true
    )
    static class AutoDetectRateLimitStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(RateLimitStore.class)
        public RateLimitStore rateLimitStore() {
            ClassLoader cl = AutoDetectRateLimitStoreConfiguration.class.getClassLoader();
            if (ClassUtils.isPresent("ext.library.redis.util.RedisUtil", cl)) {
                return new RedisRateLimitStore();
            }
            return new MemoryRateLimitStore();
        }

    }

}
