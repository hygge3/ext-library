package ext.library.cache.config;

import ext.library.cache.core.CacheAspect;
import ext.library.cache.properties.CacheProperties;
import ext.library.cache.strategy.*;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

/**
 * 缓存自动配置
 *
 * @since 2025.08.29
 */
@AutoConfiguration
@EnableConfigurationProperties({CacheProperties.class})
public class CacheAutoConfiguration {

    /**
     * 缓存切面
     *
     * @param cacheStrategy 缓存策略
     *
     * @return 缓存切面实例
     */
    @Bean
    public CacheAspect cacheAspect(CacheStrategy cacheStrategy) {
        Logs.info(EmojiSymbol.CACHE, "载入模块：缓存（Strategy: {}）", cacheStrategy.getClass().getSimpleName());
        return new CacheAspect(cacheStrategy);
    }

    // ──────────── 显式配置 ────────────

    /**
     * 显式配置：进程内内存缓存
     * <p>
     * 当 {@code ext.cache.cache-storage=CAFFEINE} 时生效。
     * <p>
     * 枚举值 {@code CAFFEINE} 仅为向后兼容保留，实际实现为 {@link InMemoryStrategy}。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = CacheProperties.PREFIX, name = "cache-storage", havingValue = "MEMORY")
    static class InMemoryStrategyConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheStrategy.class)
        public CacheStrategy cacheStrategy() {
            return new InMemoryStrategy();
        }

    }

    /**
     * 显式配置：Redis 分布式缓存
     * <p>
     * 当 {@code ext.cache.cache-storage=REDIS} 且类路径中存在 {@code ext-redis} 依赖时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.redis.util.RedisUtil")
    @ConditionalOnProperty(prefix = CacheProperties.PREFIX, name = "cache-storage", havingValue = "REDIS")
    static class RedisStrategyConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheStrategy.class)
        public CacheStrategy cacheStrategy() {
            return new RedisStrategy();
        }

    }

    /**
     * 显式配置：PostgreSQL 分布式缓存
     * <p>
     * 当 {@code ext.cache.cache-storage=POSTGRES} 且类路径中存在 {@code ext-postgres} 依赖时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.postgres.util.PostgresUtil")
    @ConditionalOnProperty(prefix = CacheProperties.PREFIX, name = "cache-storage", havingValue = "POSTGRES")
    static class PostgresStrategyConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheStrategy.class)
        public CacheStrategy cacheStrategy() {
            return new PostgresStrategy();
        }

    }

    /**
     * 显式配置：二级缓存（Caffeine + 分布式缓存）
     * <p>
     * 当 {@code ext.cache.cache-storage=L2} 时生效。
     * 分布式后端通过 {@code ext.cache.l2-backend} 配置（默认 AUTO 自动检测）。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = CacheProperties.PREFIX, name = "cache-storage", havingValue = "L2")
    static class L2StrategyConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheStrategy.class)
        public CacheStrategy cacheStrategy() {
            return new L2Strategy();
        }

    }

    // ──────────── 自动检测 ────────────

    /**
     * 自动检测缓存策略
     * <p>
     * 当 {@code ext.cache.cache-storage} 未配置或显式设置为 {@code AUTO} 时生效，
     * 根据类路径中可用的依赖自动选择：
     * <ul>
     *     <li>存在 ext-redis 或 ext-postgres → {@link L2Strategy}（内存缓存 + 分布式缓存）</li>
     *     <li>无分布式依赖 → {@link InMemoryStrategy}</li>
     * </ul>
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = CacheProperties.PREFIX,
            name = "cache-storage",
            havingValue = "AUTO",
            matchIfMissing = true
    )
    static class AutoDetectStrategyConfiguration {

        @Bean
        @ConditionalOnMissingBean(CacheStrategy.class)
        public CacheStrategy cacheStrategy() {
            ClassLoader cl = AutoDetectStrategyConfiguration.class.getClassLoader();
            boolean hasRedis = ClassUtils.isPresent("ext.library.redis.util.RedisUtil", cl);
            boolean hasPostgres = ClassUtils.isPresent("ext.library.postgres.util.PostgresUtil", cl);
            if (hasRedis || hasPostgres) {
                // L2Strategy 内部通过 l2-backend 配置（默认 AUTO）进一步决定分布式后端
                return new L2Strategy();
            }
            return new InMemoryStrategy();
        }

    }

}
