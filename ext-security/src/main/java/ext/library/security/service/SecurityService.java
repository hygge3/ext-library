package ext.library.security.service;

import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecurityLoginParams;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.security.enums.Logical;
import ext.library.security.enums.TokenState;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.listener.SecurityEventPublishManager;
import ext.library.security.properties.SecurityProperties;
import ext.library.security.repository.SecurityRepository;
import ext.library.security.util.PermissionUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ObjectUtil;
import ext.library.web.util.ServletUtil;

import jakarta.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 认证接口
 */
public class SecurityService {
    private final SecurityRepository repository;
    private final SecurityProperties properties;

    public SecurityService(SecurityRepository repository, SecurityProperties properties) {
        this.repository = repository;
        this.properties = properties;
    }

    /**
     * 裁剪 token 前缀
     *
     * @param token 令牌
     *
     * @return {@link String }
     */
    private static String cutPrefixToken(String token) {
        if (ObjectUtil.isEmpty(token)) {
            return null;
        }
        return token.replaceAll(SecurityConstant.AUTHORIZATION_PREFIX, "");
    }

    /**
     * 拼接 token 前缀
     */
    private static String appendTokenPrefix(String token) {
        return SecurityConstant.AUTHORIZATION_PREFIX + token;
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
        currentSession.flushSessionStorage();
        // 持久化保存 token
        currentSession.getCurrentSecurityToken().flushTokenStorage();
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
        session.flushSessionStorage();
        // 持久化保存 token
        currentSession.getCurrentSecurityToken().flushTokenStorage();
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
        String token = (String) ServletUtil.getRequestAttribute(SecurityConstant.SECURITY_CUSTOM_IDENTITY_TOKEN);

        if (ObjectUtil.isEmpty(token)) {
            // 尝试从请求参数中获取
            token = (String) ServletUtil.getRequestAttribute(properties.getSecurityName());
        }

        if (ObjectUtil.isEmpty(token)) {
            // 尝试从 header 头中获取
            token = ServletUtil.getHeader(properties.getSecurityName());
        }

        if (ObjectUtil.isEmpty(token)) {
            // 尝试从 cookie 中读取
            SecurityProperties.CookieProperties cookieProperties = properties.getCookieConfig();
            token = ServletUtil.getCookieValue(cookieProperties.getCookieName());
        }

        // 都没有拿到则认为没有认证登录
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
     * 检查 token 信息
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
     *
     * @param session 会话信息
     * @param prefix  错误消息前缀（"当前"或"指定"）
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
        if (!SecurityConstant.NON_EXPIRING.equals(securityToken.getActivityTimeout())) {
            Long activityTimeout = tokenActivityTimeout(securityToken.getToken());
            if (activityTimeout == null || activityTimeout <= 0) {
                throw new UnauthorizedException(prefix + " token 已超时");
            }
        }
    }

    /**
     * 踢下线操作
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
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被踢下线的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.KICKED_OFFLINE);
        session.flushSessionStorage();
        SecurityEventPublishManager.doKickOut(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 顶下线操作
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
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被顶下线的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.REPLACED_OFFLINE);
        session.flushSessionStorage();
        SecurityEventPublishManager.doReplaceOut(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 续约 token
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
            session.renewalToken(securityToken.getToken());
            SecurityEventPublishManager.doRenewal(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
        }
    }

    /**
     * 封禁 token
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
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被封禁的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.BANNED);
        session.flushSessionStorage();
        SecurityEventPublishManager.doBanned(session.getLoginId(), securityToken.getToken(), securityToken.getDeviceType());
    }

    /**
     * 解封 token
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
        SecuritySession session = ObjectUtil.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被解封的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), TokenState.NORMAL);
        session.flushSessionStorage();
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
        session.flushSessionStorage();
        if (repository.removeTokenByTokenValue(securityToken.getToken())) {
            // token 被删除通知
            SecurityEventPublishManager.doRemove(session.getLoginId(), securityToken.getToken(), deviceType);
        }
    }

    /**
     * 退出操作
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
            session.destroySecuritySession();
        } else {
            session.flushSessionStorage();
        }

        // 清理 request 中的 token 信息
        ServletUtil.removeRequestAttribute(properties.getSecurityName());

        // 清理 cookie
        if (Boolean.TRUE.equals(properties.getEnableCookie())) {
            SecurityProperties.CookieProperties cookieProperties = properties.getCookieConfig();
            ServletUtil.addCookie(cookieProperties.getCookieName(), null, 0);
            ServletUtil.addCookie(SecurityConstant.SECURITY_SESSION_ID, null, 0);
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
     * @return SecurityPagination
     */
    public List<SecuritySession> querySecuritySessionList(String tokenValue, Boolean sortedDesc) {
        List<String> list = repository.queryTokenList(tokenValue, sortedDesc);
        List<SecuritySession> resultList = new ArrayList<>();
        if (Objects.nonNull(list)) {
            // 数据处理
            list.forEach(token -> {
                SecuritySession session = getSecuritySessionByToken(token);
                if (Objects.nonNull(session)) {
                    session.setTokenInfoList(null);
                } else {
                    session = new SecuritySession();
                    SecurityToken securityToken = new SecurityToken();
                    securityToken.setToken(token);
                    session.setCurrentSecurityToken(securityToken);
                }
                resultList.add(session);
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
     * 获取 token 临时超时时间
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
        return list.parallelStream().count();
    }

    /**
     * 设置请求信息
     *
     * @param securitySession SecuritySession
     */
    private void setRequestInfo(@Nonnull SecuritySession securitySession) {
        // 清空全局存储的自定义 token，防止登录时用户传入导致登录异常
        ServletUtil.removeRequestAttribute(SecurityConstant.SECURITY_CUSTOM_IDENTITY_TOKEN);
        // 将 token 设置到请求参数中
        ServletUtil.setRequestAttribute(properties.getSecurityName(), appendTokenPrefix(securitySession.getCurrentSecurityToken().getToken()));
    }

    /**
     * 设置响应信息
     *
     * @param securitySession SecuritySession
     */
    private void setResponseInfo(@Nonnull SecuritySession securitySession) {
        // 设置 header
        ServletUtil.setHeader(properties.getSecurityName(), appendTokenPrefix(securitySession.getCurrentSecurityToken().getToken()));
        ServletUtil.addHeader("Access-Control-Expose-Headers", properties.getSecurityName());

        // 设置 cookie
        if (Boolean.TRUE.equals(properties.getEnableCookie())) {
            SecurityProperties.CookieProperties cookieProperties = properties.getCookieConfig();
            // 将 security token value 写入 cookie
            ServletUtil.addCookie(cookieProperties.getCookieName(), securitySession.getCurrentSecurityToken().getToken(), securitySession.getCurrentSecurityToken().getTimeout().intValue());
            // 将 security session id 写入 cookie
            ServletUtil.addCookie(SecurityConstant.SECURITY_SESSION_ID, securitySession.getSecuritySessionId(), securitySession.getCurrentSecurityToken().getTimeout().intValue());
        }
    }

    /**
     * 检查并设置登录信息
     *
     * @param loginId 登录 id
     * @param model   登录参数
     *
     * @return SecuritySession {@link SecuritySession}
     */
    private SecuritySession checkAndSetSecuritySession(String loginId, SecurityLoginParams model) {
        // 判断是否超过最大颁发 token 数
        if (!SecurityConstant.NON_LIMIT.equals(properties.getIssueTokenMaxLimit())) {
            long tokenCount = this.getTokenCount();
            if (properties.getIssueTokenMaxLimit() >= tokenCount) {
                throw new UnauthorizedException("颁发 token 已超过最大限制数");
            }
        }
        SecurityToken securityToken = model.convert(loginId);
        SecuritySession session = repository.getSecuritySessionByLoginId(loginId);
        if (Objects.isNull(session)) {
            session = model.convert(loginId, securityToken);
            return session;
        }

        SecuritySession securitySession = session;
        if (!model.getMountData().isEmpty()) {
            securitySession.getMountData().putAll(model.getMountData());
        }
        securitySession.setCurrentSecurityToken(securityToken);

        List<SecurityToken> availableTokenInfoList = securitySession.getTokenInfoList().stream().filter(item -> TokenState.NORMAL == item.getState()).toList();
        if (!availableTokenInfoList.isEmpty()) {
            // 最先登录的 token
            SecurityToken earliestToken = availableTokenInfoList.getFirst();
            // 验证登录设备类型数量
            if (!SecurityConstant.NON_LIMIT.equals(properties.getMaxLoginDeviceTypeLimit()) && availableTokenInfoList.stream().map(SecurityToken::getDeviceType).distinct().count() >= properties.getMaxLoginDeviceTypeLimit()) {

                availableTokenInfoList.stream().filter(item -> item.getDeviceType().equals(earliestToken.getDeviceType())).forEach(tokenInfo -> {
                    // 已达到登录设备类型上限，顶掉最先登录设备的所有相同的设备类型
                    securitySession.updateTokenInfoState(tokenInfo.getToken(), TokenState.REPLACED_OFFLINE);
                    SecurityEventPublishManager.doReplaceOut(loginId, tokenInfo.getToken(), tokenInfo.getDeviceType());
                });
            }
            if (Boolean.TRUE.equals(properties.getIsConcurrentLogin())) {
                // 允许并发，验证登录设备数量
                if (!SecurityConstant.NON_LIMIT.equals(properties.getMaxLoginLimit()) && availableTokenInfoList.size() >= properties.getMaxLoginLimit()) {
                    // 已达到登录上限，顶掉最先登录的设备
                    securitySession.updateTokenInfoState(earliestToken.getToken(), TokenState.REPLACED_OFFLINE);
                    SecurityEventPublishManager.doReplaceOut(loginId, earliestToken.getToken(), earliestToken.getDeviceType());
                }
            } else {
                // 将旧的登录信息修改为被顶下线状态
                availableTokenInfoList.stream().filter(item -> item.getDeviceType().equals(securitySession.getCurrentSecurityToken().getDeviceType())).forEach(tokenInfo -> {
                    securitySession.updateTokenInfoState(tokenInfo.getToken(), TokenState.REPLACED_OFFLINE);
                    SecurityEventPublishManager.doReplaceOut(securitySession.getLoginId(), tokenInfo.getToken(), tokenInfo.getDeviceType());
                });
            }
        }

        securitySession.setUpdateTime(LocalDateTime.now());
        securitySession.addTokenInfo(securitySession.getCurrentSecurityToken());
        return securitySession;
    }

}
