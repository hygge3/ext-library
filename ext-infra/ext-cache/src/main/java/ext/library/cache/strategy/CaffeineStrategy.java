package ext.library.cache.strategy;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

/**
 * Caffeine 本地缓存策略
 * <p>
 * 使用延迟初始化，根据配置动态创建 Cache 实例。
 * 支持基于条目的自定义过期策略和访问后刷新过期时间。
 *
 * @since 2025.08.29
 */
public class CaffeineStrategy implements CacheStrategy {

    private volatile Cache<@NonNull String, @NonNull CaffeineEntry> cache;

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
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        CaffeineEntry entry = getCache().getIfPresent(genKey(cacheName, key));
        if (entry == null) {
            return null;
        }
        return clazz.cast(entry.value());
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        String cacheKey = genKey(cacheName, key);
        getCache().put(cacheKey, new CaffeineEntry(cacheKey, value, expireTime, true));
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public void evict(String cacheName, String key) {
        getCache().invalidate(genKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        CacheProperties properties = SpringUtil.getBean(CacheProperties.class);
        String prefix = properties.getKeyPrefix() + ":" + cacheName + ":";
        getCache().asMap().keySet().removeIf(k -> k.startsWith(prefix));
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
