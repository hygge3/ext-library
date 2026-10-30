package ext.library.idempotent.config;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.cache.strategy.L2Strategy;
import ext.library.idempotent.DefaultKeyGenerator;
import ext.library.idempotent.IdempotentAspect;
import ext.library.idempotent.KeyGenerator;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 幂等模块自动配置
 * <p>
 * 幂等键的存储完全交给 [ext-cache]：本模块只依赖一个 {@link CacheStrategy} Bean，因此存储后端
 * （Caffeine / Redis / PostgreSQL）由 {@code ext.cache.cache-storage} 统一决定，不再自带 KeyStore
 * 与后端探测逻辑。
 * <p>
 * 没有 {@code CacheStrategy} 时启动直接失败（缺依赖是配置错误，不该静默降级成另一个后端）；
 * 配成 {@code L2} 同样启动即报错，原因见下面的 Bean 方法。
 */
@AutoConfiguration(afterName = "ext.library.cache.config.CacheAutoConfiguration")
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
     * <p>
     * 两级缓存不能用于幂等：本地层与分布式层各自做 {@code putIfAbsent} 会出现「本地没命中、远端已存在」
     * 的分裂判定，并发重复请求仍会被放行——这恰好是本模块唯一要保证的事，所以宁可启动失败也不含糊放行。
     *
     * @param cacheStrategy 缓存策略
     * @param keyGenerator  Key 生成器
     *
     * @return 幂等切面实例
     */
    @Bean
    public IdempotentAspect idempotentAspect(CacheStrategy cacheStrategy, KeyGenerator keyGenerator) {
        if (cacheStrategy instanceof L2Strategy) {
            throw new ExtException(
                    EmojiSymbol.IDEMPOTENT,
                    "ext-idempotent 不支持两级缓存：请将 ext.cache.cache-storage 改为 CAFFEINE / REDIS / POSTGRES"
            );
        }
        Logs.info(EmojiSymbol.IDEMPOTENT, "载入模块：幂等（存储: {}）", cacheStrategy.getClass().getSimpleName());
        return new IdempotentAspect(cacheStrategy, keyGenerator);
    }

}
