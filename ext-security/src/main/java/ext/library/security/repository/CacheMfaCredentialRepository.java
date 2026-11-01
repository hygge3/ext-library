package ext.library.security.repository;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.domain.MfaCredential;

import java.time.Duration;

/**
 * 基于 ext-cache 的两步验证凭证存储实现
 *
 * @since 4.1.1
 */
public class CacheMfaCredentialRepository implements MfaCredentialRepository {

    private static final String cacheName = "security:mfa";

    private final CacheStrategy cacheStrategy;

    public CacheMfaCredentialRepository(CacheStrategy cacheStrategy) {
        // 解绑必须全网可见，不能只清本机（同 SecurityCacheRepository）
        this.cacheStrategy = cacheStrategy.distributed();
    }

    @Override
    public MfaCredential getMfaCredential(String loginId) {
        MfaCredential credential = cacheStrategy.get(cacheName, loginId, MfaCredential.class);
        return credential == null ? null : credential.copy();
    }

    @Override
    public boolean saveMfaCredential(MfaCredential credential, Duration expireTime) {
        if (credential == null || credential.getLoginId() == null) {
            return false;
        }
        cacheStrategy.put(cacheName, credential.getLoginId(), credential, expireTime);
        return true;
    }

    @Override
    public boolean removeMfaCredential(String loginId) {
        cacheStrategy.evict(cacheName, loginId);
        return true;
    }

}
