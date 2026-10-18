package ext.library.lock.config;

import ext.library.lock.LockKeyGenerator;
import ext.library.lock.RedisLockAspect;
import ext.library.redis.util.DistributedLock;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 分布式锁自动配置
 */
@AutoConfiguration
@ConditionalOnClass(DistributedLock.class)
public class RedisLockAutoConfiguration {

    /**
     * 默认的锁 Key 生成器
     *
     * @return Key 生成器实例
     */
    @Bean
    @ConditionalOnMissingBean
    public LockKeyGenerator lockKeyGenerator() {
        return new LockKeyGenerator.Default();
    }

    /**
     * 分布式锁切面
     *
     * @param keyGenerator Key 生成器
     *
     * @return 分布式锁切面实例
     */
    @Bean
    public RedisLockAspect redisLockAspect(LockKeyGenerator keyGenerator) {
        Logs.info(EmojiSymbol.LOCK, "载入模块：分布式锁");
        return new RedisLockAspect(keyGenerator);
    }

}
