package ext.library.security.repository;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.tool.util.StringUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 基于 ext-cache 的安全存储实现
 * <p>
 * 使用 CacheStrategy 作为底层存储，支持多种缓存后端（Caffeine、Redis、PostgreSQL、L2）。
 * tokenIndex 同样持久化到 CacheStrategy，支持分布式部署。
 *
 * @since 4.0.0
 */
public class SecurityCacheRepository implements SecurityRepository {

    /**
     * Session 缓存名称
     */
    private static final String sessionCacheName = "security:session";

    /**
     * Token 缓存名称
     */
    private static final String tokenCacheName = "security:token";

    /**
     * Token 索引缓存名称（持久化到 CacheStrategy，支持分布式）
     */
    private static final String tokenIndexCacheName = "security:token:index";

    /**
     * Token 索引 key
     */
    private static final String tokenIndexKey = "all";

    private final CacheStrategy cacheStrategy;

    public SecurityCacheRepository(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    // -----------------------security session----------------------------------

    @Override
    public SecuritySession getSecuritySessionByLoginId(String loginId) {
        SecuritySession session = cacheStrategy.get(sessionCacheName, loginId, SecuritySession.class);
        if (session == null) {
            return null;
        }

        // 检查是否已过期
        if (isSessionExpired(session)) {
            removeSecuritySessionByLoginId(loginId);
            return null;
        }

        return session;
    }

    @Override
    public Long getSessionTimeoutByLoginId(String loginId) {
        SecuritySession session = getSecuritySessionByLoginId(loginId);
        if (session == null) {
            return null;
        }
        return calculateRemainingSeconds(session.getCreateTime(), session.getTimeout());
    }

    @Override
    public boolean saveSecuritySession(SecuritySession session) {
        if (session == null || session.getLoginId() == null) {
            return false;
        }

        Duration expireTime = calculateExpireDuration(session.getTimeout());
        cacheStrategy.put(sessionCacheName, session.getLoginId(), session, expireTime);
        return true;
    }

    @Override
    public boolean removeSecuritySessionByLoginId(String loginId) {
        SecuritySession session = cacheStrategy.get(sessionCacheName, loginId, SecuritySession.class);
        if (session == null) {
            return false;
        }

        // 移除该 Session 关联的所有 Token
        session.getTokenInfoList().forEach(item -> removeTokenByTokenValue(item.getToken()));

        // 移除 Session
        cacheStrategy.evict(sessionCacheName, loginId);
        return true;
    }

    // -----------------------security token----------------------------------

    @Override
    public SecurityToken getSecurityTokenByTokenValue(String tokenValue) {
        return cacheStrategy.get(tokenCacheName, tokenValue, SecurityToken.class);
    }

    @Override
    public LocalDateTime getActivityTimeByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        return token == null ? null : token.getActivityTime();
    }

    @Override
    public Long getTokenTimeOutByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        if (token == null) {
            return null;
        }
        return calculateRemainingSeconds(token.getCreateTime(), token.getTimeout());
    }

    @Override
    public Long getTokenActivityTimeOutByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        if (token == null) {
            return null;
        }
        return calculateRemainingSeconds(token.getActivityTime(), token.getActivityTimeout());
    }

    @Override
    public boolean saveToken(SecurityToken token) {
        if (token == null || token.getToken() == null) {
            return false;
        }

        Duration expireTime = calculateExpireDuration(token.getTimeout());
        cacheStrategy.put(tokenCacheName, token.getToken(), token, expireTime);

        // 更新持久化 Token 索引
        addToTokenIndex(token.getToken());
        return true;
    }

    @Override
    public boolean removeTokenByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        if (token == null) {
            removeFromTokenIndex(tokenValue);
            return false;
        }

        cacheStrategy.evict(tokenCacheName, tokenValue);
        removeFromTokenIndex(tokenValue);
        return true;
    }

    @Override
    public boolean renewalTokenByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        if (token == null) {
            return false;
        }

        token.setActivityTime(LocalDateTime.now());
        return saveToken(token);
    }

    @Override
    public List<String> queryTokenList(String tokenValue, boolean sortedDesc) {
        Set<String> index = getTokenIndex();
        List<String> result = new ArrayList<>();

        for (String key : index) {
            if (StringUtil.isNotBlank(tokenValue) && !key.contains(tokenValue)) {
                continue;
            }

            // 验证 Token 是否仍存在于缓存中
            SecurityToken token = getSecurityTokenByTokenValue(key);
            if (token != null) {
                result.add(key);
            } else {
                // 清理过期的索引条目
                removeFromTokenIndex(key);
            }
        }

        result.sort(Comparator.naturalOrder());
        if (sortedDesc) {
            Collections.reverse(result);
        }

        return result;
    }

    // -----------------------private methods----------------------------------

    /**
     * 检查 Session 是否已过期
     */
    private boolean isSessionExpired(SecuritySession session) {
        if (session.getTimeout() == null || NON_EXPIRING.equals(session.getTimeout())) {
            return false;
        }
        return isExpired(session.getCreateTime(), session.getTimeout());
    }

    /**
     * 计算过期时长（永不过期时使用 365 天）
     */
    private Duration calculateExpireDuration(Long timeoutSeconds) {
        if (timeoutSeconds == null || NON_EXPIRING.equals(timeoutSeconds)) {
            return Duration.ofDays(365);
        }
        return Duration.ofSeconds(timeoutSeconds);
    }

    /**
     * 获取持久化的 Token 索引
     */
    @SuppressWarnings("unchecked")
    private Set<String> getTokenIndex() {
        Set<String> index = cacheStrategy.get(tokenIndexCacheName, tokenIndexKey, Set.class);
        return index != null ? index : new HashSet<>();
    }

    /**
     * 向持久化索引中添加 token
     */
    private void addToTokenIndex(String tokenValue) {
        Set<String> index = getTokenIndex();
        index.add(tokenValue);
        cacheStrategy.put(tokenIndexCacheName, tokenIndexKey, index, Duration.ofDays(365));
    }

    /**
     * 从持久化索引中移除 token
     */
    private void removeFromTokenIndex(String tokenValue) {
        Set<String> index = getTokenIndex();
        if (index.remove(tokenValue)) {
            cacheStrategy.put(tokenIndexCacheName, tokenIndexKey, index, Duration.ofDays(365));
        }
    }
}
