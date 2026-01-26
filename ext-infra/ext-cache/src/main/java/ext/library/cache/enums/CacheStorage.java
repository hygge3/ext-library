package ext.library.cache.enums;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.cache.strategy.CaffeineStrategy;
import ext.library.cache.strategy.L2Strategy;
import ext.library.cache.strategy.PostgresStrategy;
import ext.library.cache.strategy.RedisStrategy;

import java.util.function.Supplier;

/**
 * 缓存存储方式
 * <p>
 * 使用 {@link Supplier} 延迟初始化策略实例，避免在 Spring 容器未就绪时创建实例。
 *
 * @since 2025.08.29
 */
public enum CacheStorage {

    /**
     * Redis 分布式缓存
     * <p>
     * 需要引入 ext-redis 模块
     */
    REDIS(RedisStrategy::new),

    /**
     * PostgreSQL 分布式缓存
     * <p>
     * 需要引入 ext-postgres 模块
     */
    POSTGRES(PostgresStrategy::new),

    /**
     * Caffeine 本地内存缓存
     */
    CAFFEINE(CaffeineStrategy::new),

    /**
     * 二级缓存（Caffeine + 分布式缓存）
     * <p>
     * 先查本地缓存，未命中再查分布式缓存（Redis 或 PostgreSQL），并回填本地缓存。
     * 分布式缓存后端通过 {@code ext.cache.l2-backend} 配置指定。
     */
    L2(L2Strategy::new);

    private final Supplier<CacheStrategy> supplier;
    private volatile CacheStrategy cached;

    CacheStorage(Supplier<CacheStrategy> supplier) {
        this.supplier = supplier;
    }

    /**
     * 获取缓存策略实例
     * <p>
     * 使用双重检查锁定确保线程安全的延迟初始化
     *
     * @return 缓存策略实例
     */
    public CacheStrategy getCacheStrategy() {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = supplier.get();
                }
            }
        }
        return cached;
    }
}
