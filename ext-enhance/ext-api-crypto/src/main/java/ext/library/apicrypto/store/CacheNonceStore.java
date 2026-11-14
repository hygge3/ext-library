package ext.library.apicrypto.store;

import ext.library.cache.strategy.CacheStrategy;

import java.time.Duration;

/**
 * 基于 ext-cache 的随机数存储
 * <p>
 * 直接复用 {@link CacheStrategy#putIfAbsent}，后端各自是原子操作：Caffeine {@code asMap().putIfAbsent}、
 * Redis {@code SET NX PX}、PostgreSQL {@code INSERT … ON CONFLICT DO NOTHING}。
 * 后端由 {@code ext.cache.cache-storage} 决定，单机（Caffeine）够用，多实例必须选 REDIS / POSTGRES / L2。
 *
 * @since 4.1.0
 */
public class CacheNonceStore implements NonceStore {

    /**
     * 缓存名
     */
    private static final String CACHE_NAME = "api-crypto:nonce";

    private final CacheStrategy cacheStrategy;

    public CacheNonceStore(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    @Override
    public boolean tryAcquire(String nonce, Duration ttl) {
        return cacheStrategy.putIfAbsent(CACHE_NAME, nonce, Boolean.TRUE, ttl);
    }

}
