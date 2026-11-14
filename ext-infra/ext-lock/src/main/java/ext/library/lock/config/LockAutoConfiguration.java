package ext.library.lock.config;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.lock.LockKeyGenerator;
import ext.library.lock.RedisLockAspect;
import ext.library.lock.store.LockStore;
import ext.library.lock.store.MemoryLockStore;
import ext.library.lock.store.PostgresLockStore;
import ext.library.lock.store.RedisLockStore;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * 分布式锁自动配置
 * <p>
 * 锁后端跟随 {@code ext.cache.storage} 解析出的 {@link CacheStrategy#backendType()}，本模块不再有自己的
 * {@code store-type}：同一个中间件只该配一次，两处各配一遍必然在部署时配漂。
 * <p>
 * {@code MEMORY} 只对单实例有效，多实例必须选 {@code REDIS} 或 {@code POSTGRES}。
 */
@AutoConfiguration
public class LockAutoConfiguration {

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
     * 按缓存后端选择锁实现
     *
     * @param cacheStrategy 缓存策略，只用来读取后端类型
     * @param beanFactory   Bean 工厂
     *
     * @return 锁存储
     */
    @Bean
    @ConditionalOnMissingBean(LockStore.class)
    public LockStore lockStore(CacheStrategy cacheStrategy, BeanFactory beanFactory) {
        return switch (cacheStrategy.backendType()) {
            case MEMORY -> new MemoryLockStore();
            case REDIS -> new RedisLockStore();
            case POSTGRES -> new PostgresLockStore(resolveDataSource(beanFactory));
        };
    }

    /**
     * 分布式锁切面
     *
     * @param lockStore    锁存储
     * @param keyGenerator Key 生成器
     *
     * @return 分布式锁切面实例
     */
    @Bean
    public RedisLockAspect redisLockAspect(LockStore lockStore, LockKeyGenerator keyGenerator) {
        Logs.info(EmojiSymbol.LOCK, "载入模块：分布式锁（存储: {}）", lockStore.getClass().getSimpleName());
        return new RedisLockAspect(lockStore, keyGenerator);
    }

    /**
     * 取 PostgreSQL 咨询锁用的数据源
     * <p>
     * 优先用 ext-postgres 的专用数据源 {@code postgresDataSource}，没有则回退到应用主数据源。
     *
     * @param beanFactory Bean 工厂
     *
     * @return 数据源
     */
    private static DataSource resolveDataSource(BeanFactory beanFactory) {
        if (beanFactory.containsBean("postgresDataSource")) {
            return beanFactory.getBean("postgresDataSource", DataSource.class);
        }
        DataSource dataSource = beanFactory.getBeanProvider(DataSource.class).getIfAvailable();
        if (dataSource == null) {
            throw new ExtException(EmojiSymbol.LOCK, "ext-lock 使用 PostgreSQL 咨询锁需要 DataSource");
        }
        return dataSource;
    }

}
