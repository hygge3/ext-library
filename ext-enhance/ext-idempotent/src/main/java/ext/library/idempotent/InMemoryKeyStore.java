package ext.library.idempotent;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

/**
 * 基于内存的幂等 Key 存储实现
 * <p>
 * 使用 Caffeine 缓存实现，支持按 key 独立设置过期时间。
 * 适用于单机部署场景。
 */
public class InMemoryKeyStore implements KeyStore {

    /**
     * 默认最大缓存条目数
     */
    private static final int DEFAULT_MAXIMUM_SIZE = 10_000;

    private final Cache<@NonNull String, Long> cache;

    public InMemoryKeyStore() {
        this(DEFAULT_MAXIMUM_SIZE);
    }

    public InMemoryKeyStore(int maximumSize) {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<@NonNull String, @NonNull Long>() {
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
    public synchronized boolean saveIfAbsent(String key, Duration duration) {
        Long value = cache.getIfPresent(key);
        if (value == null) {
            cache.policy()
                    .expireVariably()
                    .ifPresent(policy -> policy.put(key, duration.toNanos(), duration));
            return true;
        }
        return false;
    }

    @Override
    public void remove(String key) {
        cache.invalidate(key);
    }

}
