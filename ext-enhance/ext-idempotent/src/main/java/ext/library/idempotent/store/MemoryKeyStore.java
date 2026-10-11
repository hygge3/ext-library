package ext.library.idempotent.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;

import java.time.Duration;

/**
 * 基于内存的幂等 Key 存储实现
 * <p>
 * 使用 Caffeine 缓存实现，支持按 key 独立设置过期时间。
 * 适用于单机部署场景。
 */
public class MemoryKeyStore implements KeyStore {

    /**
     * 默认最大缓存条目数
     */
    private static final int defaultMaximumSize = 10_000;

    private final Cache<String, Long> cache;

    public MemoryKeyStore() {
        this(defaultMaximumSize);
    }

    public MemoryKeyStore(int maximumSize) {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, Long>() {
                    @Override
                    public long expireAfterCreate(String key, Long durationNanos, long currentTime) {
                        return durationNanos;
                    }

                    @Override
                    public long expireAfterUpdate(String key, Long durationNanos, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(String key, Long durationNanos, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .maximumSize(maximumSize)
                .scheduler(Scheduler.systemScheduler())
                .build();
    }

    @Override
    public boolean saveIfAbsent(String key, Duration duration) {
        return cache.asMap().putIfAbsent(key, duration.toNanos()) == null;
    }

    @Override
    public void remove(String key) {
        cache.invalidate(key);
    }

}
