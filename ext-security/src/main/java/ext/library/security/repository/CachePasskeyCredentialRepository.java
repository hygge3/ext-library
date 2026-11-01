package ext.library.security.repository;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.domain.PasskeyCredential;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 ext-cache 的通行密钥凭证存储实现
 * <p>
 * 凭证按 credentialId 存一份，另按 loginId 维护一份凭证 ID 索引。
 *
 * @since 4.1.1
 */
public class CachePasskeyCredentialRepository implements PasskeyCredentialRepository {

    private static final String credentialCacheName = "security:passkey";
    private static final String indexCacheName = "security:passkey:index";
    private static final Duration expireTime = Duration.ofDays(365);

    private final CacheStrategy cacheStrategy;

    public CachePasskeyCredentialRepository(CacheStrategy cacheStrategy) {
        // 删除凭证必须全网可见，不能只清本机（同 SecurityCacheRepository）
        this.cacheStrategy = cacheStrategy.distributed();
    }

    @Override
    public List<PasskeyCredential> listPasskeyCredentials(String loginId) {
        List<PasskeyCredential> result = new ArrayList<>();
        for (String credentialId : getIndex(loginId)) {
            PasskeyCredential credential = getPasskeyCredential(credentialId);
            if (credential != null) {
                result.add(credential);
            } else {
                removeFromIndex(loginId, credentialId);
            }
        }
        return result;
    }

    @Override
    public PasskeyCredential getPasskeyCredential(String credentialId) {
        PasskeyCredential credential = cacheStrategy.get(credentialCacheName, credentialId, PasskeyCredential.class);
        return credential == null ? null : credential.copy();
    }

    @Override
    public boolean savePasskeyCredential(PasskeyCredential credential) {
        if (credential == null || credential.getLoginId() == null || credential.getCredentialId() == null) {
            return false;
        }
        cacheStrategy.put(credentialCacheName, credential.getCredentialId(), credential, expireTime);
        addToIndex(credential.getLoginId(), credential.getCredentialId());
        return true;
    }

    @Override
    public boolean removePasskeyCredential(String loginId, String credentialId) {
        cacheStrategy.evict(credentialCacheName, credentialId);
        removeFromIndex(loginId, credentialId);
        return true;
    }

    // region 索引

    @SuppressWarnings("unchecked")
    private Set<String> getIndex(String loginId) {
        Set<String> index = cacheStrategy.get(indexCacheName, loginId, Set.class);
        // 复制后修改：缓存的集合实例不能被多个请求同时改
        return index != null ? new HashSet<>(index) : new HashSet<>();
    }

    private void addToIndex(String loginId, String credentialId) {
        Set<String> index = getIndex(loginId);
        if (index.add(credentialId)) {
            cacheStrategy.put(indexCacheName, loginId, index, expireTime);
        }
    }

    private void removeFromIndex(String loginId, String credentialId) {
        Set<String> index = getIndex(loginId);
        if (index.remove(credentialId)) {
            cacheStrategy.put(indexCacheName, loginId, index, expireTime);
        }
    }

    // endregion
}
