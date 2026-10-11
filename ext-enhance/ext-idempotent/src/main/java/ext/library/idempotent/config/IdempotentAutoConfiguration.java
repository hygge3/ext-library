package ext.library.idempotent.config;

import ext.library.idempotent.DefaultKeyGenerator;
import ext.library.idempotent.IdempotentAspect;
import ext.library.idempotent.KeyGenerator;
import ext.library.idempotent.store.KeyStore;
import ext.library.idempotent.store.MemoryKeyStore;
import ext.library.idempotent.store.PostgresKeyStore;
import ext.library.idempotent.store.RedisKeyStore;
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
 * 幂等模块自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(IdempotentProperties.class)
public class IdempotentAutoConfiguration {

    /**
     * 默认的幂等 Key 生成器
     *
     * @return Key 生成器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyGenerator idempotentKeyGenerator() {
        return new DefaultKeyGenerator();
    }

    /**
     * 幂等切面
     *
     * @param keyStore     Key 存储器
     * @param keyGenerator Key 生成器
     *
     * @return 幂等切面实例
     */
    @Bean
    public IdempotentAspect idempotentAspect(KeyStore keyStore, KeyGenerator keyGenerator) {
        Logs.info(EmojiSymbol.IDEMPOTENT, "载入模块：幂等（KeyStore: {}）", keyStore.getClass().getSimpleName());
        return new IdempotentAspect(keyStore, keyGenerator);
    }

    // ──────────── 显式配置 ────────────

    /**
     * 显式配置：Redis 存储
     * <p>
     * 当 {@code ext.idempotent.key-store-type=REDIS} 且类路径中存在 {@code ext-redis} 依赖时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.redis.util.RedisUtil")
    @ConditionalOnProperty(prefix = IdempotentProperties.PREFIX, name = "key-store-type", havingValue = "REDIS")
    static class RedisKeyStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(KeyStore.class)
        public KeyStore idempotentKeyStore() {
            return new RedisKeyStore();
        }

    }

    /**
     * 显式配置：PostgreSQL 存储
     * <p>
     * 当 {@code ext.idempotent.key-store-type=POSTGRES} 且类路径中存在 {@code ext-postgres} 依赖时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.postgres.util.PostgresUtil")
    @ConditionalOnProperty(prefix = IdempotentProperties.PREFIX, name = "key-store-type", havingValue = "POSTGRES")
    static class PostgresKeyStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(KeyStore.class)
        public KeyStore idempotentKeyStore() {
            return new PostgresKeyStore();
        }

    }

    /**
     * 显式配置：内存存储
     * <p>
     * 当 {@code ext.idempotent.key-store-type=MEMORY} 时生效。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = IdempotentProperties.PREFIX, name = "key-store-type", havingValue = "MEMORY")
    static class MemoryKeyStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(KeyStore.class)
        public KeyStore idempotentKeyStore() {
            return new MemoryKeyStore();
        }

    }

    // ──────────── 自动检测 ────────────

    /**
     * 自动检测 KeyStore 实现
     * <p>
     * 当 {@code ext.idempotent.key-store-type} 未配置或显式设置为 {@code AUTO} 时生效，
     * 根据类路径中可用的依赖自动选择存储实现，优先级：Redis > PostgreSQL > Memory。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = IdempotentProperties.PREFIX,
            name = "key-store-type",
            havingValue = "AUTO",
            matchIfMissing = true
    )
    static class AutoDetectKeyStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(KeyStore.class)
        public KeyStore idempotentKeyStore() {
            ClassLoader cl = AutoDetectKeyStoreConfiguration.class.getClassLoader();
            if (ClassUtils.isPresent("ext.library.redis.util.RedisUtil", cl)) {
                return new RedisKeyStore();
            }
            if (ClassUtils.isPresent("ext.library.postgres.util.PostgresUtil", cl)) {
                return new PostgresKeyStore();
            }
            return new MemoryKeyStore();
        }

    }

}
