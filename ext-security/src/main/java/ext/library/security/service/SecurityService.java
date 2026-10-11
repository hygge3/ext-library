package ext.library.security.service;

import ext.library.security.domain.SecurityLoginParams;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.security.enums.Logical;
import ext.library.security.enums.TokenState;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.listener.SecurityEventPublishManager;
import ext.library.security.properties.CookieProperties;
import ext.library.security.properties.SecurityProperties;
import ext.library.security.repository.SecurityRepository;
import ext.library.security.util.PermissionUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.web.util.ServletUtil;

import jakarta.annotation.Nonnull;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 认证接口
 */
public class SecurityService {
    /**
     * 不限制（-1）
     */
    private static final Integer nonLimit = -1;
    /**
     * 不过期（-1L）
     */
    private static final Long nonExpiring = -1L;
    /**
     * 无效 Token 清理阈值：48 小时
     */
    private static final Duration invalidTokenCleanup = Duration.ofHours(48);
    /**
     * 自定义 Token 参数名称
     */
    private static final String securityCustomIdentityToken = "security_custom_identity_token";
    /**
     * Security Session ID
     */
    private static final String securitySessionId = "security_session_id";

    private final SecurityRepository repository;
    private final SecurityProperties properties;

    public SecurityService(SecurityRepository repository, SecurityProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * 裁剪 token 前缀
     */
    private String cutPrefixToken(String token) {
        if (ObjectUtil.isEmpty(token)) {
            return null;
        }
        return token.replaceAll(properties.getTokenPrefix(), "");
    }

    /**
     * 拼接 token 前缀
     */
    private String appendTokenPrefix(String token) {
        return properties.getTokenPrefix() + token;
    }

    /**
     * 登录方法
     *
     * @param loginId 登录 Id
     * @param model   登录参数
     */
    public void doLogin(String loginId, SecurityLoginParams model) {
        // 检查并设置 SecuritySession 信息
        SecuritySession currentSession = checkAndSetSecuritySession(loginId, model);
        // 持久化保存 session
        flushSessionStorage(currentSession);
        // 持久化保存 token
        saveToken(currentSession.getCurrentSecurityToken());
        // 存储 token 到 request 参数中
        setRequestInfo(currentSession);
        // 将参数设置响应 response 中
        setResponseInfo(currentSession);
        // 发布登录操作事件
        SecurityEventPublishManager.doLogin(loginId, currentSession.getCurrentSecurityToken().getToken(), model);
    }

    /**
     * 创建指定账号的登录信息
     *
     * @param loginId    登录 Id
     * @param loginModel 登录参数
     *
     * @return token
     */
    public String createLoginByLoginId(String loginId, SecurityLoginParams loginModel) {
        SecuritySession currentSession = getCurrentSecuritySession();
        if (currentSession.getLoginId().equals(loginId)) {
            throw new ExtException(EmojiSymbol.SECURITY, "创建指定账号的登录 ID:{} 不能与当前登录 ID:{} 相同", loginId, currentSession.getLoginId());
        }
        // 检查并设置 SecuritySession 信息
        SecuritySession session = checkAndSetSecuritySession(loginId, loginModel);
        // 持久化保存 session
        flushSessionStorage(session);
        // 持久化保存 token
        saveToken(session.getCurrentSecurityToken());
        return session.getCurrentSecurityToken().getToken();
    }

    /**
     * 根据 token 获取 SecuritySession 信息
     *
     * @param token 用户 token
     *
     * @return SecuritySession
     */
    public SecuritySession getSecuritySessionByToken(String token) {
        SecurityToken securityToken = repository.getSecurityTokenByTokenValue(token);
        if (Objects.isNull(securityToken) || ObjectUtil.isEmpty(securityToken.getLoginId())) {
            return null;
        }
        SecuritySession securitySession = getSecuritySessionByLoginId(securityToken.getLoginId());
        if (Objects.isNull(securitySession)) {
            return null;
        }
        // 设置当前的 token 信息
        securitySession.setCurrentSecurityToken(securityToken);
        return securitySession;
    }

    /**
     * 根据 loginId 获取 SecuritySession 信息
     *
     * @param loginId 用户登录 Id
     *
     * @return SecuritySession
     */
    public SecuritySession getSecuritySessionByLoginId(String loginId) {
        return repository.getSecuritySessionByLoginId(loginId);
    }

    /**
     * 获取当前的 SecuritySession 信息
     *
     * @return SecuritySession
     */
    public SecuritySession getCurrentSecuritySession() {
        // 优先内部设置自定义参数获取，仅框架内部传参使用
        String token = (String) ServletUtil.getRequestAttribute(securityCustomIdentityToken);

        if (ObjectUtil.isEmpty(token)) {
            token = (String) ServletUtil.getRequestAttribute(properties.getTokenName());
        }

        if (ObjectUtil.isEmpty(token)) {
            token = ServletUtil.getHeader(properties.getTokenName());
        }

        if (ObjectUtil.isEmpty(token)) {
            CookieProperties cookieProperties = properties.getCookieConfig();
            token = ServletUtil.getCookieValue(cookieProperties.getCookieName());
        }

        if (ObjectUtil.isEmpty(token)) {
            throw new UnauthorizedException("未登录认证");
        }

        token = cutPrefixToken(token);
        SecuritySession securitySession = getSecuritySessionByToken(token);
        if (Objects.isNull(securitySession) || Objects.isNull(securitySession.getCurrentSecurityToken())) {
            throw new UnauthorizedException("无效的认证信息");
        }
        return securitySession;
    }

    /**
     * 检查 token 信息（当前请求）
     */
    public void checkToken() {
        SecuritySession session = getCurrentSecuritySession();
        validateTokenState(session, "当前");

        // 自动续约
        if (Boolean.TRUE.equals(properties.getAutoRenewal())) {
            renewalToken(session.getCurrentSecurityToken().getToken());
        }
    }

    /**
     * 检查 token 信息
     *
     * @param token 用户 token
     */
    public void checkToken(String token) {
        SecuritySession session = getSecuritySessionByToken(token);
        validateTokenState(session, "指定");
    }

    /**
     * 验证 Token 状态
     */
    private void validateTokenState(SecuritySession session, String prefix) {
        if (session == null || session.getCurrentSecurityToken() == null) {
            throw new UnauthorizedException(prefix + " token 已失效");
        }

        SecurityToken securityToken = session.getCurrentSecurityToken();
        TokenState state = securityToken.getState();

        if (state == TokenState.KICKED_OFFLINE) {
            throw new UnauthorizedException(prefix + " token 已被踢下线");
        }
        if (state == TokenState.REPLACED_OFFLINE) {
            throw new UnauthorizedException(prefix + " token 已被顶下线");
        }
        if (state == TokenState.BANNED) {
            throw new UnauthorizedException(prefix + " token 已被封禁");
        }

        // 检查活跃超时
        if (!nonExpiring.equals(securityToken.getActivityTimeout())) {
            Long activityTimeout = tokenActivityTimeout(securityToken.getToken());
            if (activityTimeout == null || activityTimeout <= 0) {
                throw new UnauthorizedException(prefix + " token 已超时");
            }
        }
    }

    /**
     * 踢下线操作（当前 token）
     */
    public void kickOut() {
        kickOut(null);
    }

    /**
     * 踢下线操作
     *
     * @param token 用户 token
     */
    public void kickOut(String token) {
        SecuritySession session = resolveSession(token, "需要被踢下线的 token 无效");
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.KICKED_OFFLINE);
        flushSessionStorage(session);
        SecurityEventPublishManager.doKickOut(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 顶下线操作（当前 token）
     */
    public void replaceOut() {
        replaceOut(null);
    }

    /**
     * 顶下线操作
     *
     * @param token 用户 token
     */
    public void replaceOut(String token) {
        SecuritySession session = resolveSession(token, "需要被顶下线的 token 无效");
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.REPLACED_OFFLINE);
        flushSessionStorage(session);
        SecurityEventPublishManager.doReplaceOut(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 续约 token（当前 token）
     */
    public void renewalToken() {
        renewalToken(null);
    }

    /**
     * 续约 token
     * <p>
     * 只有当距离上次续约时间超过配置的间隔时才会执行续约操作
     *
     * @param token 用户 token
     */
    public void renewalToken(String token) {
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (session == null || session.getCurrentSecurityToken() == null) {
            return;
        }

        SecurityToken securityToken = session.getCurrentSecurityToken();
        LocalDateTime lastActivityTime = tokenLastActivityTime(securityToken.getToken());
        if (lastActivityTime == null) {
            return;
        }

        // 检查是否超过续约间隔
        long intervalSeconds = properties.getAutoRenewalIntervalSeconds();
        LocalDateTime nextRenewalTime = lastActivityTime.plusSeconds(intervalSeconds);
        if (nextRenewalTime.isBefore(LocalDateTime.now())) {
            if (repository.renewalTokenByTokenValue(securityToken.getToken())) {
                Logs.debug(EmojiSymbol.SECURITY, "续约成功：{}", securityToken.getToken());
                SecurityEventPublishManager.doRenewal(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
            }
        }
    }

    /**
     * 封禁 token（当前 token）
     */
    public void bannedToken() {
        bannedToken(null);
    }

    /**
     * 封禁 token
     *
     * @param token 用户 token
     */
    public void bannedToken(String token) {
        SecuritySession session = resolveSession(token, "需要被封禁的 token 无效");
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.BANNED);
        flushSessionStorage(session);
        SecurityEventPublishManager.doBanned(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 解封 token（当前 token）
     */
    public void unsealToken() {
        unsealToken(null);
    }

    /**
     * 解封 token
     *
     * @param token 用户 token
     */
    public void unsealToken(String token) {
        SecuritySession session = resolveSession(token, "需要被解封的 token 无效");
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.NORMAL);
        flushSessionStorage(session);
        SecurityEventPublishManager.doUnseal(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 删除 token
     *
     * @param token 用户 token
     */
    public void removeToken(String token) {
        SecuritySession session = getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被删除的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        String deviceType = securityToken.getDeviceType();
        session.removeTokenInfo(securityToken.getToken());
        flushSessionStorage(session);
        if (repository.removeTokenByTokenValue(securityToken.getToken())) {
            SecurityEventPublishManager.doRemove(session.getLoginId(), securityToken.getToken(), deviceType);
        }
    }

    /**
     * 退出操作（当前 token）
     */
    public void logout() {
        logout(null);
    }

    /**
     * 退出操作
     *
     * @param token 用户 token
     */
    public void logout(String token) {
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被退出的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        String deviceType = securityToken.getDeviceType();
        session.removeTokenInfo(securityToken.getToken());

        // 判断 token list 是否为空，如果为空则销毁此 session
        if (session.getTokenInfoList().isEmpty()) {
            destroySecuritySession(session);
        } else {
            flushSessionStorage(session);
        }

        // 清理 request 中的 token 信息
        ServletUtil.removeRequestAttribute(properties.getTokenName());

        // 清理 cookie
        if (Boolean.TRUE.equals(properties.getEnableCookie())) {
            CookieProperties cookieProperties = properties.getCookieConfig();
            ServletUtil.addCookie(cookieProperties.getCookieName(), null, 0);
            ServletUtil.addCookie(securitySessionId, null, 0);
        }

        if (repository.removeTokenByTokenValue(securityToken.getToken())) {
            SecurityEventPublishManager.doLogout(session.getLoginId(), securityToken.getToken(), deviceType);
        }
    }

    /**
     * 查询 SecuritySession 列表
     *
     * @param tokenValue token 值，支持模糊匹配
     * @param sortedDesc 是否降序
     *
     * @return List<SecuritySession>
     */
    public List<SecuritySession> querySecuritySessionList(String tokenValue, Boolean sortedDesc) {
        List<String> list = repository.queryTokenList(tokenValue, sortedDesc);
        List<SecuritySession> resultList = new ArrayList<>();
        if (Objects.nonNull(list)) {
            list.forEach(t -> {
                SecuritySession s = getSecuritySessionByToken(t);
                if (Objects.nonNull(s)) {
                    s.setTokenInfoList(null);
                } else {
                    s = new SecuritySession();
                    SecurityToken securityToken = new SecurityToken();
                    securityToken.setToken(t);
                    s.setCurrentSecurityToken(securityToken);
                }
                resultList.add(s);
            });
        }
        return resultList;
    }

    /**
     * 查询 token 值列表
     *
     * @param tokenValue token 值
     * @param sortedDesc 是否降序
     *
     * @return List<String>
     */
    public List<String> queryTokenValueList(String tokenValue, Boolean sortedDesc) {
        return repository.queryTokenList(tokenValue, sortedDesc);
    }

    /**
     * 获取 session 超时时间
     *
     * @param loginId 登录 Id
     *
     * @return 时长秒 -1 表示永久有效
     */
    public Long sessionTimeout(String loginId) {
        return repository.getSessionTimeoutByLoginId(loginId);
    }

    /**
     * 获取 token 超时时间
     *
     * @param token tokenValue
     *
     * @return 时长秒 -1 表示永久有效
     */
    public Long tokenTimeout(String token) {
        return repository.getTokenTimeOutByTokenValue(token);
    }

    /**
     * 获取 token 活跃超时时间
     *
     * @param token tokenValue
     *
     * @return 时长秒 -1 表示永久有效
     */
    public Long tokenActivityTimeout(String token) {
        return repository.getTokenActivityTimeOutByTokenValue(token);
    }

    /**
     * 获取 token 的最新续约时间
     *
     * @param token tokenValue
     *
     * @return 续约时间
     */
    public LocalDateTime tokenLastActivityTime(String token) {
        return repository.getActivityTimeByTokenValue(token);
    }

    /**
     * 当前用户是否有指定角色
     *
     * @param roleCode 角色码
     *
     * @return true 有 false 没有
     */
    public boolean hasRole(String roleCode) {
        return PermissionUtil.hasRole(roleCode);
    }

    /**
     * 当前用户是否有指定角色
     *
     * @param roleCode 角色码
     * @param logical  条件
     *
     * @return true 有 false 没有
     */
    public Boolean hasRole(String[] roleCode, Logical logical) {
        return PermissionUtil.hasMultiPermValid(List.of(roleCode), logical, PermissionUtil.getRoles());
    }

    /**
     * 当前用户是否有指定权限码
     *
     * @param permissionCode 权限码
     *
     * @return true 有 false 没有
     */
    public Boolean hasPermission(String permissionCode) {
        return PermissionUtil.hasPermission(permissionCode);
    }

    /**
     * 当前用户是否有指定权限码
     *
     * @param permissionCode 权限码
     * @param logical        条件
     *
     * @return true 有 false 没有
     */
    public Boolean hasPermission(String[] permissionCode, Logical logical) {
        return PermissionUtil.hasMultiPermValid(List.of(permissionCode), logical, PermissionUtil.getPermissions());
    }

    /**
     * 是否已登录
     *
     * @return true 登录 false 未登录
     */
    public Boolean isLogin() {
        try {
            checkToken();
        } catch (UnauthorizedException e) {
            return false;
        }
        return true;
    }

    /**
     * token 数量统计
     *
     * @return 总数
     */
    public Long getTokenCount() {
        List<String> list = repository.queryTokenList(null, true);
        if (Objects.isNull(list)) {
            return 0L;
        }
        return (long) list.size();
    }

    // ==================== 私有方法 ====================

    /**
     * 解析 session，token 为空时取当前请求的 session
     */
    private SecuritySession resolveSession(String token, String errorMsg) {
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException(errorMsg);
        }
        return session;
    }

    /**
     * 持久化保存 session，并同步清理无效 token
     */
    private void flushSessionStorage(@Nonnull SecuritySession session) {
        // 清理无效 Token
        List<SecurityToken> invalidTokenList = new ArrayList<>();
        if (!session.getTokenInfoList().isEmpty()) {
            session.getTokenInfoList().forEach(securityToken -> {
                // 非正常状态且超过阈值时间的 Token 视为无效
                if (securityToken.getUpdateTime() != null
                        && securityToken.getState() != TokenState.NORMAL
                        && securityToken.getUpdateTime().plus(invalidTokenCleanup).isBefore(LocalDateTime.now())) {
                    invalidTokenList.add(securityToken);
                }

                // 同步 Token 状态
                SecurityToken storedToken = repository.getSecurityTokenByTokenValue(securityToken.getToken());
                if (storedToken != null) {
                    if (securityToken.getState() != storedToken.getState()) {
                        storedToken.setState(securityToken.getState());
                        repository.saveToken(storedToken);
                    }
                    securityToken.setActivityTime(storedToken.getActivityTime());
                }
            });
        }

        // 移除无效 Token（内存）
        invalidTokenList.forEach(item -> session.removeTokenInfo(item.getToken()));

        // 保存 Session
        boolean result = repository.saveSecuritySession(session);
        if (!result) {
            throw new ExtException(EmojiSymbol.SECURITY, "保存 Session 认证数据失败");
        }

        // 清理无效 Token 存储
        invalidTokenList.forEach(item -> {
            if (repository.removeTokenByTokenValue(item.getToken())) {
                SecurityEventPublishManager.doRemove(session.getLoginId(), item.getToken(), item.getDeviceType());
            }
        });
    }

    /**
     * 持久化保存 token
     */
    private void saveToken(@Nonnull SecurityToken token) {
        boolean result = repository.saveToken(token);
        if (!result) {
            throw new ExtException(EmojiSymbol.SECURITY, "保存 Token 认证数据失败");
        }
    }

    /**
     * 销毁 SecuritySession
     */
    private void destroySecuritySession(@Nonnull SecuritySession session) {
        if (Objects.isNull(session.getLoginId())) {
            return;
        }
        if (repository.removeSecuritySessionByLoginId(session.getLoginId())) {
            SecurityEventPublishManager.doDestroySecuritySession(session.getSecuritySessionId());
        }
    }

    /**
     * 设置请求信息
     */
    private void setRequestInfo(@Nonnull SecuritySession securitySession) {
        ServletUtil.removeRequestAttribute(securityCustomIdentityToken);
        ServletUtil.setRequestAttribute(properties.getTokenName(), appendTokenPrefix(securitySession.getCurrentSecurityToken().getToken()));
    }

    /**
     * 设置响应信息
     */
    private void setResponseInfo(@Nonnull SecuritySession securitySession) {
        ServletUtil.setHeader(properties.getTokenName(), appendTokenPrefix(securitySession.getCurrentSecurityToken().getToken()));
        ServletUtil.addHeader("Access-Control-Expose-Headers", properties.getTokenName());

        if (Boolean.TRUE.equals(properties.getEnableCookie())) {
            CookieProperties cookieProperties = properties.getCookieConfig();
            ServletUtil.addCookie(cookieProperties.getCookieName(), securitySession.getCurrentSecurityToken().getToken(), securitySession.getCurrentSecurityToken().getTimeout().intValue());
            ServletUtil.addCookie(securitySessionId, securitySession.getSecuritySessionId(), securitySession.getCurrentSecurityToken().getTimeout().intValue());
        }
    }

    /**
     * 检查并设置登录信息
     */
    private SecuritySession checkAndSetSecuritySession(String loginId, SecurityLoginParams model) {
        // 判断是否超过最大颁发 token 数
        if (!nonLimit.equals(properties.getIssueTokenMaxLimit())) {
            long tokenCount = this.getTokenCount();
            if (tokenCount >= properties.getIssueTokenMaxLimit()) {
                throw new UnauthorizedException("颁发 token 已超过最大限制数");
            }
        }

        SecurityToken securityToken = model.convert(loginId, properties);
        SecuritySession session = repository.getSecuritySessionByLoginId(loginId);
        if (Objects.isNull(session)) {
            return model.convert(loginId, securityToken, properties);
        }

        if (!model.getMountData().isEmpty()) {
            session.getMountData().putAll(model.getMountData());
        }
        session.setCurrentSecurityToken(securityToken);

        List<SecurityToken> availableTokenInfoList = session.getTokenInfoList().stream()
                .filter(item -> TokenState.NORMAL == item.getState()).toList();

        if (!availableTokenInfoList.isEmpty()) {
            SecurityToken earliestToken = availableTokenInfoList.getFirst();

            // 验证登录设备类型数量
            if (!nonLimit.equals(properties.getMaxLoginDeviceTypeLimit())
                    && availableTokenInfoList.stream().map(SecurityToken::getDeviceType).distinct().count() >= properties.getMaxLoginDeviceTypeLimit()) {
                availableTokenInfoList.stream()
                        .filter(item -> item.getDeviceType().equals(earliestToken.getDeviceType()))
                        .forEach(tokenInfo -> {
                            session.updateTokenInfoState(tokenInfo.getToken(), TokenState.REPLACED_OFFLINE);
                            SecurityEventPublishManager.doReplaceOut(loginId, tokenInfo.getToken(), tokenInfo.getDeviceType());
                        });
            }

            if (Boolean.TRUE.equals(properties.getIsConcurrentLogin())) {
                // 允许并发，验证登录设备数量
                if (!nonLimit.equals(properties.getMaxLoginLimit()) && availableTokenInfoList.size() >= properties.getMaxLoginLimit()) {
                    session.updateTokenInfoState(earliestToken.getToken(), TokenState.REPLACED_OFFLINE);
                    SecurityEventPublishManager.doReplaceOut(loginId, earliestToken.getToken(), earliestToken.getDeviceType());
                }
            } else {
                // 不允许并发，顶掉同设备类型的旧 token
                availableTokenInfoList.stream()
                        .filter(item -> item.getDeviceType().equals(session.getCurrentSecurityToken().getDeviceType()))
                        .forEach(tokenInfo -> {
                            session.updateTokenInfoState(tokenInfo.getToken(), TokenState.REPLACED_OFFLINE);
                            SecurityEventPublishManager.doReplaceOut(session.getLoginId(), tokenInfo.getToken(), tokenInfo.getDeviceType());
                        });
            }
        }

        session.setUpdateTime(LocalDateTime.now());
        session.addTokenInfo(session.getCurrentSecurityToken());
        return session;
    }
}
