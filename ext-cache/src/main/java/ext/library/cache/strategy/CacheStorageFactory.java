package ext.library.cache.strategy;

import ext.library.cache.enums.CacheStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheStorageFactory {
    private static final Map<String, CacheStrategy> CACHE_STRATEGY_MAP = new ConcurrentHashMap<>(3);

    public static CacheStrategy getCacheStrategy(CacheStorage cacheStorage) {
        return CACHE_STRATEGY_MAP.computeIfAbsent(cacheStorage.name(), k -> switch (cacheStorage) {
            case REDIS -> new RedisStrategy();
            case L2 -> new L2Strategy();
            case CAFFEINE -> new CaffeineStrategy();
        });
    }
}