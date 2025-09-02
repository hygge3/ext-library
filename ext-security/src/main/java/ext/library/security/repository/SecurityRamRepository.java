package ext.library.security.repository;

import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.tool.core.Exceptions;
import ext.library.tool.util.DateUtil;
import ext.library.tool.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Nonnull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * 默认基于内存存储实现
 * </p>
 */
@Slf4j
public class SecurityRamRepository implements SecurityRepository {

    /**
     * SecuritySession 存储
     */
    private static final AtomicReference<Map<String, SecuritySession>> sessionMap = new AtomicReference<>(
            new ConcurrentHashMap<>());

    /**
     * SecurityToken 存储
     */
    private static final AtomicReference<Map<String, SecurityToken>> tokenMap = new AtomicReference<>(
            new ConcurrentHashMap<>());

    /**
     * 记录数据版本信息
     */
    private static final AtomicReference<Map<String, Long>> versionMap = new AtomicReference<>(
            new ConcurrentHashMap<>());

    @Override
    public SecuritySession getSecuritySessionByLoginId(String loginId) {
        SecuritySession securitySession = sessionMap.get().get(loginId);
        if (null == securitySession) {
            return null;
        }
        Long timeout = securitySession.getTimeout();
        if (!SecurityConstant.NON_EXPIRING.equals(timeout)) {
            // 判断是否超时
            String createTime = securitySession.getCreateTime();
            if (DateUtil.parse(createTime).plusSeconds(timeout).isBefore(LocalDateTime.now())) {
                // 已超时，销毁 security session
                securitySession.destroySecuritySession();
                return null;
            }
        }
        Long version = versionMap.get().get(loginId);
        securitySession.setVersion(null == version ? 0L : version);
        return securitySession;
    }

    @Override
    public Long getSessionTimeoutByLoginId(String loginId) {
        SecuritySession securitySession = getSecuritySessionByLoginId(loginId);
        if (null == securitySession) {
            return null;
        }
        Long timeout = securitySession.getTimeout();
        if (!SecurityConstant.NON_EXPIRING.equals(timeout)) {
            // 计算剩余时间
            String createTime = securitySession.getCreateTime();
            long second = DateUtil.parse(createTime).plusSeconds(timeout).toEpochSecond(ZoneOffset.ofHours(8))
                    - LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
            return second > 0 ? second : 0L;
        }
        return timeout;
    }

    @Override
    public boolean saveSecuritySession(@Nonnull SecuritySession session) {
        Long version = versionMap.get().get(session.getLoginId());
        version = null == version ? 0L : version;
        if (null != session.getVersion() && !session.getVersion().equals(version)) {
            throw Exceptions.throwOut("[🛡️] 版本验证异常");
        }
        versionMap.get().put(session.getLoginId(), ++version);
        sessionMap.get().put(session.getLoginId(), session);
        return true;
    }

    @Override
    public boolean removeSecuritySessionByLoginId(String loginId) {
        SecuritySession securitySession = sessionMap.get().get(loginId);
        if (null == securitySession) {
            return false;
        }
        securitySession.getTokenInfoList().forEach(item -> removeTokenByTokenValue(item.getToken()));
        sessionMap.get().remove(loginId);
        return true;
    }

    @Override
    public SecurityToken getSecurityTokenByTokenValue(String tokenValue) {
        return tokenMap.get().get(tokenValue);
    }

    @Override
    public String getActivityTimeByTokenValue(String tokenValue) {
        SecurityToken securityToken = getSecurityTokenByTokenValue(tokenValue);
        return Objects.isNull(securityToken) ? null : securityToken.getActivityTime();
    }

    @Override
    public Long getTokenTimeOutByTokenValue(String tokenValue) {
        SecurityToken securityToken = getSecurityTokenByTokenValue(tokenValue);
        if (Objects.isNull(securityToken)) {
            return null;
        }
        String createTime = securityToken.getCreateTime();
        // 计算超时时间
        Long timeout = securityToken.getTimeout();
        if (SecurityConstant.NON_EXPIRING.equals(timeout)) {
            return SecurityConstant.NON_EXPIRING;
        }
        // 计算剩余时间
        long second = DateUtil.parse(createTime).plusSeconds(timeout).toEpochSecond(ZoneOffset.ofHours(8))
                - LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        return second > 0 ? second : 0L;
    }

    @Override
    public Long getTokenActivityTimeOutByTokenValue(String tokenValue) {
        SecurityToken securityToken = getSecurityTokenByTokenValue(tokenValue);
        if (Objects.isNull(securityToken)) {
            return null;
        }
        String activityTime = securityToken.getActivityTime();
        // 计算超时时间
        Long timeout = securityToken.getActivityTimeout();
        if (SecurityConstant.NON_EXPIRING.equals(timeout)) {
            return SecurityConstant.NON_EXPIRING;
        }
        // 计算剩余时间
        long second = DateUtil.parse(activityTime).plusSeconds(timeout).toEpochSecond(ZoneOffset.ofHours(8))
                - LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(8));
        return second > 0 ? second : 0L;
    }

    @Override
    public boolean saveToken(SecurityToken token) {
        tokenMap.get().put(token.getToken(), token);
        return true;
    }

    @Override
    public boolean removeTokenByTokenValue(String tokenValue) {
        SecurityToken securityToken = tokenMap.get().remove(tokenValue);
        return Objects.nonNull(securityToken);
    }

    @Override
    public boolean renewalTokenByTokenValue(String tokenValue) {
        SecurityToken securityToken = tokenMap.get().remove(tokenValue);
        securityToken.setActivityTime(DateUtil.format(LocalDateTime.now()));
        return true;
    }

    @Override
    public List<String> queryTokenList(String tokenValue, boolean sortedDesc) {
        List<String> list = new ArrayList<>();
        for (String key : tokenMap.get().keySet()) {
            if (StringUtil.isNotBlank(tokenValue) && !key.contains(tokenValue)) {
                continue;
            }
            list.add(key);
        }
        if (sortedDesc) {
            Collections.reverse(list);
        }
        return list;
    }

}