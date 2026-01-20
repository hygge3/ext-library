package ext.library.idempotent.config;

import ext.library.idempotent.aspect.IdempotentAspect;
import ext.library.idempotent.key.generator.DefaultIdempotentKeyGenerator;
import ext.library.idempotent.key.generator.IdempotentKeyGenerator;
import ext.library.idempotent.key.store.IdempotentKeyStore;
import ext.library.idempotent.key.store.InMemoryIdempotentKeyStore;
import ext.library.idempotent.key.store.RedisIdempotentKeyStore;
import ext.library.idempotent.properties.IdempotentProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 幂等自动装配
 */
@AutoConfiguration
@EnableConfigurationProperties(IdempotentProperties.class)
public class IdempotentAutoConfig {

    /**
     * 默认的幂等前缀生成器
     *
     * @return 幂等 Key 生成器
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentKeyGenerator idempotentKeyGenerator() {
        return new DefaultIdempotentKeyGenerator();
    }

    /**
     * 默认的幂等键存储器
     *
     * @param properties 幂等属性配置
     *
     * @return 幂等 Key 存储
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentKeyStore idempotentKeyStore(IdempotentProperties properties) {
        IdempotentProperties.KeyStoreType keyStoreType = properties.getKeyStoreType();
        if (keyStoreType.equals(IdempotentProperties.KeyStoreType.REDIS)) {
            return new RedisIdempotentKeyStore();
        } else {
            return new InMemoryIdempotentKeyStore();
        }
    }

    /**
     * 幂等切面
     *
     * @param idempotentKeyStore 幂等 key 仓库
     *
     * @return IdempotentAspect
     */
    @Bean
    public IdempotentAspect idempotentAspect(IdempotentKeyStore idempotentKeyStore, IdempotentKeyGenerator idempotentKeyGenerator) {
        Logs.info(EmojiSymbol.IDEMPOTENT, "幂等模块载入");
        return new IdempotentAspect(idempotentKeyStore, idempotentKeyGenerator);
    }

}
