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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

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
     * 默认的幂等 Key 存储器
     *
     * @param properties         幂等属性配置
     * @param jdbcClientProvider PostgreSQL JdbcClient 提供者（可选）
     *
     * @return Key 存储器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public KeyStore idempotentKeyStore(IdempotentProperties properties) {
        return switch (properties.getKeyStoreType()) {
            case REDIS -> new RedisKeyStore();
            case MEMORY -> new MemoryKeyStore();
            case POSTGRES -> new PostgresKeyStore();
        };
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
        Logs.info(EmojiSymbol.IDEMPOTENT, "载入模块：幂等");
        return new IdempotentAspect(keyStore, keyGenerator);
    }

}
