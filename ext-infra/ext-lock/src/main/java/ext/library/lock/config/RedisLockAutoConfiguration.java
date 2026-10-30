package ext.library.lock.config;

import ext.library.lock.LockKeyGenerator;
import ext.library.lock.RedisLockAspect;
import ext.library.lock.enums.LockStoreType;
import ext.library.lock.store.LockStore;
import ext.library.lock.store.PostgresLockStore;
import ext.library.lock.store.RedisLockStore;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;

/**
 * 分布式锁自动配置
 * <p>
 * 锁后端由 {@code ext.lock.store-type} 选择：{@code AUTO}（默认，按类路径优先 Redis）/ {@code REDIS} /
 * {@code POSTGRES}。{@code ext-redis} 与 {@code ext-postgres} 都是 {@code compileOnly}，消费方引哪个就用哪个；
 * 一个都没引时装配阶段直接抛异常，不静默降级成内存锁——多实例下内存锁等于没有锁。
 */
@AutoConfiguration
@EnableConfigurationProperties(LockProperties.class)
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

    // ──────────── 显式配置 ────────────

    /**
     * 显式配置：Redis 后端
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.redis.util.RedisUtil")
    @ConditionalOnProperty(prefix = LockProperties.PREFIX, name = "store-type", havingValue = "REDIS")
    static class RedisLockStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(LockStore.class)
        public LockStore lockStore() {
            return new RedisLockStore();
        }

    }

    /**
     * 显式配置：PostgreSQL 咨询锁后端
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "ext.library.postgres.util.PostgresUtil")
    @ConditionalOnProperty(prefix = LockProperties.PREFIX, name = "store-type", havingValue = "POSTGRES")
    static class PostgresLockStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(LockStore.class)
        public LockStore lockStore(BeanFactory beanFactory) {
            return new PostgresLockStore(resolveDataSource(beanFactory));
        }

    }

    // ──────────── 自动检测 ────────────

    /**
     * 自动检测：按类路径优先 Redis，其次 PostgreSQL，都没有则装配失败
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(
            prefix = LockProperties.PREFIX,
            name = "store-type",
            havingValue = "AUTO",
            matchIfMissing = true
    )
    static class AutoDetectLockStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(LockStore.class)
        public LockStore lockStore(BeanFactory beanFactory) {
            ClassLoader cl = AutoDetectLockStoreConfiguration.class.getClassLoader();
            if (ClassUtils.isPresent("ext.library.redis.util.RedisUtil", cl)) {
                return new RedisLockStore();
            }
            if (ClassUtils.isPresent("ext.library.postgres.util.PostgresUtil", cl)) {
                return new PostgresLockStore(resolveDataSource(beanFactory));
            }
            throw new ExtException(
                    EmojiSymbol.LOCK,
                    "ext-lock 未找到可用的锁后端：请引入 ext-redis 或 ext-postgres，或自定义一个 LockStore Bean"
            );
        }

    }

    /**
     * 取分布式锁用的数据源
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
            throw new ExtException(EmojiSymbol.LOCK, "ext-lock 的 PostgreSQL 后端需要一个 DataSource，但容器里没有");
        }
        return dataSource;
    }

}
