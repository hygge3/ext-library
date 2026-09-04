package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内内存缓存策略
 * <p>
 * 基于 {@link ConcurrentHashMap} 的本地缓存，无外部依赖。
 * <ul>
 *     <li>过期：惰性删除（读取时检查并清理），无后台清理线程</li>
 *     <li>容量：硬上限，超限时淘汰最先 put 的条目（FIFO）</li>
 *     <li>线程安全：依赖 ConcurrentHashMap 的原子操作</li>
 * </ul>
 *
 * @since 2025.08.29
 */
public class InMemoryStrategy implements CacheStrategy {

    private final Map<String, MemoryEntry> store = new ConcurrentHashMap<>();

    private record MemoryEntry(String key, Object value, long expireAtNanos) {

        boolean expired() {
            return expireAtNanos > 0 && System.nanoTime() >= expireAtNanos;
        }
    }

    private long maxSize() {
        return SpringUtil.getBean(CacheProperties.class).getCaffeine().getMaximumSize();
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        String cacheKey = genKey(cacheName, key);
        MemoryEntry entry = store.get(cacheKey);
        if (entry == null) {
            return null;
        }
        if (entry.expired()) {
            store.remove(cacheKey, entry);
            return null;
        }
        return clazz.cast(entry.value());
    }

    @Override
    public <T> T put(String cacheName, String key, T value, Duration expireTime) {
        String cacheKey = genKey(cacheName, key);
        long nanos = expireTime == null ? 0L : System.nanoTime() + expireTime.toNanos();
        store.put(cacheKey, new MemoryEntry(cacheKey, value, nanos));
        evictIfOverLimit();
        return value;
    }

    @Override
    public <T> T put(String cacheName, String key, T value) {
        return put(cacheName, key, value, getDefaultExpireTime());
    }

    @Override
    public void evict(String cacheName, String key) {
        store.remove(genKey(cacheName, key));
    }

    @Override
    public void clear(String cacheName) {
        String prefix = SpringUtil.getBean(CacheProperties.class).getKeyPrefix() + ":" + cacheName + ":";
        store.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * 容量超限时淘汰最先 put 的条目（FIFO）。
     * <p>
     * ponytail: 简化实现，未实现真 LRU；如需 LRU 行为，升级到 {@code LinkedHashMap} + accessOrder 或引入 Caffeine。
     */
    private void evictIfOverLimit() {
        long max = maxSize();
        if (max <= 0 || store.size() <= max) {
            return;
        }
        Iterator<Map.Entry<String, MemoryEntry>> it = store.entrySet().iterator();
        while (it.hasNext() && store.size() > max) {
            it.next();
            it.remove();
        }
    }

}