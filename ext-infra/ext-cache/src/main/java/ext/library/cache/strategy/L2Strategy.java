package ext.library.cache.strategy;

import ext.library.core.backend.BackendType;
import ext.library.core.token.TokenBucketResult;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Type;
import java.time.Duration;

/**
 * 二级缓存策略
 * <p>
 * 结合 Caffeine 本地缓存和分布式缓存（Redis 或 PostgreSQL），实现多级缓存架构。
 * 读取时先查询本地缓存，未命中则查询分布式缓存并回填本地缓存。
 * <p>
 * 分布式缓存后端按类路径自动选择，优先 Redis，其次 PostgreSQL：L2 是「本地 + 分布式」
 * 组合策略，再给一个 {@code l2-backend} 选型键会让两条分布式分支变成两个配置入口，
 * 而 {@code ext.cache.storage} 已经是唯一的选型处。
 *
 * @since 2025.08.29
 */
public class L2Strategy implements CacheStrategy {

    private final CacheStrategy caffeineStrategy = new CaffeineStrategy();

    private volatile CacheStrategy distributedStrategy;

    /**
     * 获取分布式缓存策略（延迟初始化）
     */
    public CacheStrategy getDistributedStrategy() {
        if (distributedStrategy == null) {
            synchronized (this) {
                if (distributedStrategy == null) {
                    distributedStrategy = createDistributedStrategy();
                    Logs.info(EmojiSymbol.CACHE, "L2 缓存后端: {}", distributedStrategy.backendType());
                }
            }
        }
        return distributedStrategy;
    }

    @Override
    public CacheStrategy distributed() {
        return getDistributedStrategy();
    }

    /**
     * 按类路径创建分布式缓存策略，优先级：Redis > PostgreSQL。
     */
    private CacheStrategy createDistributedStrategy() {
        ClassLoader cl = getClass().getClassLoader();
        if (ClassUtils.isPresent("ext.library.redis.util.RedisUtil", cl)) {
            return new RedisStrategy();
        }
        if (ClassUtils.isPresent("ext.library.postgres.util.PostgresUtil", cl)) {
            return new PostgresStrategy();
        }
        throw new IllegalStateException(
                "L2 后端自动检测失败：类路径中未找到 ext-redis 或 ext-postgres 依赖，" +
                "请引入相应模块或将 ext.cache.cache-storage 改为 CAFFEINE");
    }

    @Override
    public <T> T get(String cacheName, String key, Type type) {
        // 先查询 Caffeine 本地缓存
        T caffeineCache = caffeineStrategy.get(cacheName, key, type);
        if (caffeineCache != null) {
            Logs.debug(EmojiSymbol.CACHE, "从 Caffeine 中获取数据");
            return caffeineCache;
        }

        // 本地未命中，查询分布式缓存
        CacheStrategy distributed = getDistributedStrategy();
        T distributedCache = distributed.get(cacheName, key, type);
        if (distributedCache != null) {
            Logs.debug(EmojiSymbol.CACHE, "从 {} 获取数据", distributed.backendType());
            // 回填本地缓存时沿用分布式层的剩余有效期，不能用默认值：那会让本地副本比分布式层活得久，
            // 分布式层过期后本实例还在供应旧值
            Duration remaining = distributed.remainingTtl(cacheName, key);
            caffeineStrategy.put(cacheName, key, distributedCache, remaining != null ? remaining : getDefaultExpireTime());
            return distributedCache;
        }

        return null;
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        getDistributedStrategy().put(cacheName, key, value, expireTime);
        caffeineStrategy.put(cacheName, key, value, expireTime);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        getDistributedStrategy().put(cacheName, key, value);
        caffeineStrategy.put(cacheName, key, value);
        return value;
    }

    /**
     * 两级缓存的原子写入
     * <p>
     * 以分布式层为唯一裁决者：多实例共享的是它，本地层只是它的读缓存。
     * 分布式层写入成功后再回填本地，避免「本地未命中、远端已存在」的分裂判定。
     */
    @Override
    public boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime) {
        boolean created = getDistributedStrategy().putIfAbsent(cacheName, key, value, expireTime);
        if (created) {
            caffeineStrategy.put(cacheName, key, value, expireTime);
        }
        return created;
    }

    /**
     * 计数只落分布式层
     * <p>
     * 本地层也累加会双计，且各实例的本地计数互不可见，分布式下等于每台机器各自限流。
     */
    @Override
    public long increment(String cacheName, String key, long delta, Duration window) {
        return getDistributedStrategy().increment(cacheName, key, delta, window);
    }

    /**
     * 令牌桶只在分布式层：本地层的桶各实例互不可见，等于每台机器各限一份。
     */
    @Override
    public TokenBucketResult tryAcquire(String cacheName, String key, long permits, long capacity, Duration refillPeriod) {
        return getDistributedStrategy().tryAcquire(cacheName, key, permits, capacity, refillPeriod);
    }

    @Override
    public BackendType backendType() {
        return getDistributedStrategy().backendType();
    }

    @Override
    public void evict(String cacheName, String key) {
        getDistributedStrategy().evict(cacheName, key);
        caffeineStrategy.evict(cacheName, key);
    }

    @Override
    public void clear(String cacheName) {
        getDistributedStrategy().clear(cacheName);
        caffeineStrategy.clear(cacheName);
    }

}
