package ext.library.cache.enums;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.cache.strategy.CaffeineStrategy;
import ext.library.cache.strategy.L2Strategy;
import ext.library.cache.strategy.RedisStrategy;

public enum CacheStorage {
    /** redis */ REDIS(new RedisStrategy()),
    /** caffeine 内存 */ CAFFEINE(new CaffeineStrategy()),
    /** 二级缓存 caffeine+redis */ L2(new L2Strategy()),
    ;

    private final CacheStrategy cacheStrategy;

    CacheStorage(CacheStrategy cacheStrategy) {this.cacheStrategy = cacheStrategy;}

    public CacheStrategy getCacheStrategy() {
        return cacheStrategy;
    }
}