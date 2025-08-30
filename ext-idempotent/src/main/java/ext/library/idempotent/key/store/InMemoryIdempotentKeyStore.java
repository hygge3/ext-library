package ext.library.idempotent.key.store;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

/**
 * 基于内存的幂等 Key 存储组件
 */
public class InMemoryIdempotentKeyStore implements IdempotentKeyStore {

    private final Cache<@NonNull String, Long> cache;

    public InMemoryIdempotentKeyStore() {
        this.cache = Caffeine.newBuilder().expireAfter(new Expiry<@NonNull String, @NonNull Long>() {
            @Override
            public long expireAfterCreate(String key, Long emp, long currentTime) {
                return emp;
            }

            @Override
            public long expireAfterUpdate(String key, Long emp, long currentTime, long currentDuration) {
                return currentDuration;
            }

            @Override
            public long expireAfterRead(String key, Long emp, long currentTime, long currentDuration) {
                return currentDuration;
            }
        }).maximumSize(10_000).scheduler(Scheduler.systemScheduler()).build();

    }

    @Override
    public synchronized boolean saveIfAbsent(String key, Duration duration) {
        Long value = this.cache.getIfPresent(key);
        if (value == null) {
            cache.policy()
                    .expireVariably()
                    .ifPresent(e -> e.put(key, System.currentTimeMillis(), duration));
            return true;
        }
        return false;
    }

    @Override
    public void remove(String key) {
        this.cache.invalidate(key);
    }

}