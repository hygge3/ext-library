package ext.library.security.repository;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.tool.util.StringUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 ext-cache 的安全存储实现
 * <p>
 * 使用 CacheStrategy 作为底层存储，支持多种缓存后端（Caffeine、Redis、PostgreSQL、L2）。
 *
 * @since 4.0.0
 */
public class SecurityCacheRepository implements SecurityRepository {

    /**
     * Session 缓存名称
     */
    private static final String SESSION_CACHE_NAME = "security:session";

    /**
     * Token 缓存名称
     */
    private static final String TOKEN_CACHE_NAME = "security:token";

    /**
     * Token 索引（用于 queryTokenList）
     */
    private final Set<String> tokenIndex = ConcurrentHashMap.newKeySet();

    private final CacheStrategy cacheStrategy;

    public SecurityCacheRepository(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    // -----------------------security session----------------------------------

    @Override
    public SecuritySession getSecuritySessionByLoginId(String loginId) {
        SecuritySession session = cacheStrategy.get(SESSION_CACHE_NAME, loginId, SecuritySession.class);
        if (session == null) {
            return null;
        }

        // 检查是否已过期
        if (isSessionExpired(session)) {
            session.destroySecuritySession();
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
        return SecurityConstant.calculateRemainingSeconds(session.getCreateTime(), session.getTimeout());
    }

    @Override
    public boolean saveSecuritySession(SecuritySession session) {
        if (session == null || session.getLoginId() == null) {
            return false;
        }

        Duration expireTime = calculateExpireDuration(session.getTimeout());
        cacheStrategy.put(SESSION_CACHE_NAME, session.getLoginId(), session, expireTime);
        return true;
    }

    @Override
    public boolean removeSecuritySessionByLoginId(String loginId) {
        SecuritySession session = getSecuritySessionByLoginId(loginId);
        if (session == null) {
            return false;
        }

        // 移除该 Session 关联的所有 Token
        session.getTokenInfoList().forEach(item -> removeTokenByTokenValue(item.getToken()));

        // 移除 Session
        cacheStrategy.evict(SESSION_CACHE_NAME, loginId);
        return true;
    }

    // -----------------------security token----------------------------------

    @Override
    public SecurityToken getSecurityTokenByTokenValue(String tokenValue) {
        return cacheStrategy.get(TOKEN_CACHE_NAME, tokenValue, SecurityToken.class);
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
        return SecurityConstant.calculateRemainingSeconds(token.getCreateTime(), token.getTimeout());
    }

    @Override
    public Long getTokenActivityTimeOutByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        if (token == null) {
            return null;
        }
        return SecurityConstant.calculateRemainingSeconds(token.getActivityTime(), token.getActivityTimeout());
    }

    @Override
    public boolean saveToken(SecurityToken token) {
        if (token == null || token.getToken() == null) {
            return false;
        }

        Duration expireTime = calculateExpireDuration(token.getTimeout());
        cacheStrategy.put(TOKEN_CACHE_NAME, token.getToken(), token, expireTime);

        // 更新 Token 索引
        tokenIndex.add(token.getToken());
        return true;
    }

    @Override
    public boolean removeTokenByTokenValue(String tokenValue) {
        SecurityToken token = getSecurityTokenByTokenValue(tokenValue);
        if (token == null) {
            // 仍尝试从索引中移除
            tokenIndex.remove(tokenValue);
            return false;
        }

        cacheStrategy.evict(TOKEN_CACHE_NAME, tokenValue);
        tokenIndex.remove(tokenValue);
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
        List<String> result = new ArrayList<>();

        for (String key : tokenIndex) {
            // 按 tokenValue 过滤
            if (StringUtil.isNotBlank(tokenValue) && !key.contains(tokenValue)) {
                continue;
            }

            // 验证 Token 是否仍存在于缓存中
            SecurityToken token = getSecurityTokenByTokenValue(key);
            if (token != null) {
                result.add(key);
            } else {
                // 清理过期的索引条目
                tokenIndex.remove(key);
            }
        }

        // 排序
        result.sort(Comparator.naturalOrder());
        if (sortedDesc) {
            Collections.reverse(result);
        }

        return result;
    }

    // -----------------------private methods----------------------------------

    /**
     * 检查 Session 是否已过期
     *
     * @param session 会话信息
     * @return true 已过期，false 未过期
     */
    private boolean isSessionExpired(SecuritySession session) {
        if (session.getTimeout() == null || SecurityConstant.NON_EXPIRING.equals(session.getTimeout())) {
            return false;
        }
        return SecurityConstant.isExpired(session.getCreateTime(), session.getTimeout());
    }

    /**
     * 计算过期时长
     *
     * @param timeoutSeconds 超时秒数
     * @return 过期时长
     */
    private Duration calculateExpireDuration(Long timeoutSeconds) {
        if (timeoutSeconds == null || SecurityConstant.NON_EXPIRING.equals(timeoutSeconds)) {
            // 永不过期时使用 365 天作为缓存时长
            return Duration.ofDays(365);
        }
        return Duration.ofSeconds(timeoutSeconds);
    }
}
