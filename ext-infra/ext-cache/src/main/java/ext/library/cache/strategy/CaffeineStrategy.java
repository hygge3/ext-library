package ext.library.cache.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import ext.library.cache.properties.CacheProperties;
import ext.library.core.backend.BackendType;
import ext.library.core.token.TokenBucketResult;
import ext.library.core.util.SpringUtil;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Caffeine 本地缓存策略
 * <p>
 * 使用延迟初始化，根据配置动态创建 Cache 实例。
 * 支持基于条目的自定义过期策略和访问后刷新过期时间。
 *
 * @since 2025.08.29
 */
public class CaffeineStrategy implements CacheStrategy {

    /**
     * 计数器清理间隔：计数器不走 Caffeine 的过期清理，靠定期扫一遍避免 key 无限堆积
     */
    private static final long SWEEP_INTERVAL = 128;

    private volatile Cache<@NonNull String, @NonNull CaffeineEntry> cache;

    /**
     * 计数器与缓存条目分开存：缓存 {@code put} 会重置过期时间，计数器不重置
     */
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    private final AtomicLong incrementCalls = new AtomicLong();

    /**
     * 令牌桶的 GCRA 状态（理论到达时间，纳秒）
     * <p>
     * 单独一个 Caffeine 容器以复用 maximumSize 上限：桶被淘汰即回到满桶，相当于多放行一次突发。
     */
    private volatile Cache<@NonNull String, @NonNull AtomicLong> bucketCache;

    private Cache<@NonNull String, @NonNull AtomicLong> getBucketCache() {
        if (bucketCache == null) {
            synchronized (this) {
                if (bucketCache == null) {
                    bucketCache = Caffeine.newBuilder()
                            .maximumSize(SpringUtil.getBean(CacheProperties.class).getCaffeine().getMaximumSize())
                            .build();
                }
            }
        }
        return bucketCache;
    }

    /**
     * 获取或创建 Cache 实例（双重检查锁定延迟初始化）
     */
    private Cache<@NonNull String, @NonNull CaffeineEntry> getCache() {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = buildCache();
                }
            }
        }
        return cache;
    }

    /**
     * 根据配置构建 Caffeine Cache 实例
     */
    private Cache<@NonNull String, @NonNull CaffeineEntry> buildCache() {
        CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
        CacheProperties.CaffeineConfig config = properties.getCaffeine();
        boolean refreshOnAccess = config.isRefreshOnAccess();

        return Caffeine.newBuilder()
                .maximumSize(config.getMaximumSize())
                .expireAfter(new Expiry<@NonNull String, @NonNull CaffeineEntry>() {
                    @Override
                    public long expireAfterCreate(@NonNull String key, @NonNull CaffeineEntry entry, long currentTime) {
                        return entry.expireTime().toNanos();
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull String key, @NonNull CaffeineEntry entry,
                                                  long currentTime, long currentDuration) {
                        return entry.expireTime().toNanos();
                    }

                    @Override
                    public long expireAfterRead(@NonNull String key, @NonNull CaffeineEntry entry,
                                                long currentTime, long currentDuration) {
                        if (refreshOnAccess && entry.accessFresh()) {
                            return entry.expireTime().toNanos();
                        }
                        return currentDuration;
                    }
                })
                .build();
    }

    @Override
    public <T> T get(String cacheName, String key, Type type) {
        CaffeineEntry entry = getCache().getIfPresent(genKey(cacheName, key));
        return entry == null ? null : narrow(entry.value(), type);
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        expireTime = withJitter(expireTime);
        String cacheKey = genKey(cacheName, key);
        getCache().put(cacheKey, new CaffeineEntry(cacheKey, value, expireTime, true));
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime) {
        String cacheKey = genKey(cacheName, key);
        return getCache().asMap().putIfAbsent(cacheKey, new CaffeineEntry(cacheKey, value, expireTime, true)) == null;
    }

    @Override
    public long increment(String cacheName, String key, long delta, Duration window) {
        String cacheKey = genKey(cacheName, key);
        long now = System.nanoTime();
        Counter counter = counters.compute(cacheKey, (k, old) ->
                old == null || old.expireAtNanos() <= now
                        ? new Counter(delta, now + window.toNanos())
                        : old.add(delta));

        if (incrementCalls.incrementAndGet() % SWEEP_INTERVAL == 0) {
            counters.values().removeIf(c -> c.expireAtNanos() <= now);
        }
        return counter.count();
    }

    @Override
    public TokenBucketResult tryAcquire(String cacheName, String key, long permits, long capacity, Duration refillPeriod) {
        long interval = Math.max(1, refillPeriod.toNanos() / capacity);
        long tolerance = interval * (capacity - permits);
        long now = System.nanoTime();
        AtomicLong tat = getBucketCache().get(genKey(cacheName, key), k -> new AtomicLong(now));
        synchronized (tat) {
            // 推进前判定：可用令牌 = capacity - (tat - now) / interval，所以 tat - now <= tolerance 时才有 permits 个
            long base = Math.max(tat.get(), now);
            long ahead = base - now;
            if (ahead > tolerance) {
                return TokenBucketResult.of(false, ahead, interval, permits, capacity);
            }
            long next = base + interval * permits;
            tat.set(next);
            return TokenBucketResult.of(true, next - now, interval, permits, capacity);
        }
    }

    @Override
    public BackendType backendType() {
        return BackendType.MEMORY;
    }

    @Override
    public void evict(String cacheName, String key) {
        String cacheKey = genKey(cacheName, key);
        getCache().invalidate(cacheKey);
        counters.remove(cacheKey);
    }

    @Override
    public void clear(String cacheName) {
        CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
        String prefix = properties.getKeyPrefix() + ":" + cacheName + ":";
        getCache().asMap().keySet().removeIf(k -> k.startsWith(prefix));
        counters.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * 固定窗口计数器
     *
     * @param count        当前计数
     * @param expireAtNanos 窗口结束时刻（{@link System#nanoTime()}）
     */
    record Counter(long count, long expireAtNanos) {

        Counter add(long delta) {
            return new Counter(count + delta, expireAtNanos);
        }
    }

    /**
     * Caffeine 缓存条目
     *
     * @param key         缓存键
     * @param value       缓存值
     * @param expireTime  过期时间
     * @param accessFresh 读取后是否刷新过期时间
     */
    record CaffeineEntry(String key, Object value, Duration expireTime, boolean accessFresh) {
    }
}
