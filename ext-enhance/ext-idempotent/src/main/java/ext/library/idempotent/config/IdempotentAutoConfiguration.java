package ext.library.idempotent.config;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.idempotent.DefaultKeyGenerator;
import ext.library.idempotent.IdempotentAspect;
import ext.library.idempotent.KeyGenerator;
import ext.library.tool.constant.EmojiSymbol;
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
 * 没有 {@code CacheStrategy} 时启动直接失败（缺依赖是配置错误，不该静默降级成另一个后端）。
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
     * 两级缓存同样可用：{@code L2Strategy.putIfAbsent} 以分布式层为唯一裁决者，
     * 本地层只在写入成功后回填，不会出现「本地没命中、远端已存在」的分裂判定。
     *
     * @param cacheStrategy 缓存策略
     * @param keyGenerator  Key 生成器
     *
     * @return 幂等切面实例
     */
    @Bean
    public IdempotentAspect idempotentAspect(CacheStrategy cacheStrategy, KeyGenerator keyGenerator) {
        Logs.info(EmojiSymbol.IDEMPOTENT, "载入模块：幂等（存储: {}）", cacheStrategy.getClass().getSimpleName());
        return new IdempotentAspect(cacheStrategy, keyGenerator);
    }

}
