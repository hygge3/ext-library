package ext.library.cache.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

/**
 * 咖啡因策略
 *
 * @since 2025.08.29
 */
public class CaffeineStrategy implements CacheStrategy {
    private static final Cache<@NonNull String, @NonNull CaffeineEntry> CACHE = Caffeine.newBuilder().maximumSize(100).expireAfter(new Expiry<@NonNull String, @NonNull CaffeineEntry>() {
        @Override
        public long expireAfterCreate(@NonNull String s, @NonNull CaffeineEntry caffeineEntry, long l) {
            return caffeineEntry.expireTime().toNanos();
        }

        @Override
        public long expireAfterUpdate(@NonNull String s, @NonNull CaffeineEntry caffeineEntry, long l, long l1) {
            return caffeineEntry.expireTime().toNanos();
        }

        @Override
        public long expireAfterRead(@NonNull String s, @NonNull CaffeineEntry caffeineEntry, long l, long l1) {
            if (caffeineEntry.accessFresh()) {
                return caffeineEntry.expireTime().toNanos();
            }
            return l1;
        }
    }).build();

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        return clazz.cast(CACHE.getIfPresent(genKey(cacheName, key)));
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        String genKey = genKey(cacheName, key);
        CaffeineEntry caffeineEntry = new CaffeineEntry(genKey, value, expireTime, true);
        CACHE.put(genKey, caffeineEntry);
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        put(cacheName, key, value, getDefaultExpireTime());
        return value;
    }

    @Override
    public void evict(String cacheName, String key) {
        CACHE.invalidate(genKey(cacheName, key));
    }

    /**
     * 清理所有缓存
     *
     * @param cacheName 缓存名称
     */
    @Override
    public void clear(String cacheName) {
        CACHE.invalidateAll();
    }

    record CaffeineEntry(String key, Object value,
                         // 过期时间
                         Duration expireTime,
                         // 读后是否刷新
                         boolean accessFresh) {}
}