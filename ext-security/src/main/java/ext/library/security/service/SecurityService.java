package ext.library.security.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import ext.library.core.util.ServletUtil;
import ext.library.core.util.SpringUtil;
import ext.library.security.config.properties.SecurityProperties;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecurityLoginParams;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.security.enums.Logical;
import ext.library.security.exception.UnauthorizedException;
import ext.library.security.function.IdentitySwitchFunction;
import ext.library.security.listener.SecurityEventPublishManager;
import ext.library.security.repository.SecurityRepository;
import ext.library.security.util.PermissionUtil;
import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import org.jetbrains.annotations.Nullable;

/**
 * 认证接口
 */
public interface SecurityService {

    SecurityRepository REPOSITORY = SpringUtil.getBean(SecurityRepository.class);

    SecurityProperties PROPERTIES = SpringUtil.getBean(SecurityProperties.class);

    /**
     * 登录方法
     *
     * @param loginId 登录 Id
     * @param model   登录参数
     */
    default void doLogin(String loginId, SecurityLoginParams model) {
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
     * @return token
     */
    default String createLoginByLoginId(String loginId, SecurityLoginParams loginModel) {
        SecuritySession currentSession = getCurrentSecuritySession();
        if (currentSession.getLoginId().equals(loginId)) {
            throw Exceptions.throwOut("创建指定账号的登录 Id 不能与当前登录 Id 相同");
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
     * @return SecuritySession
     */
    default SecuritySession getSecuritySessionByToken(String token) {
        SecurityToken securityToken = REPOSITORY.getSecurityTokenByTokenValue(token);
        if (Objects.isNull(securityToken) || $.isEmpty(securityToken.getLoginId())) {
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
     * @return SecuritySession
     */
    default SecuritySession getSecuritySessionByLoginId(String loginId) {
        return REPOSITORY.getSecuritySessionByLoginId(loginId);
    }

    /**
     * 获取当前的 SecuritySession 信息
     *
     * @return SecuritySession
     */
    default SecuritySession getCurrentSecuritySession() {
        // 优先内部设置自定义参数获取，仅框架内部传参使用
        String token = (String) ServletUtil.getRequestAttribute(SecurityConstant.SECURITY_CUSTOM_IDENTITY_TOKEN);

        if ($.isEmpty(token)) {
            // 尝试从请求参数中获取
            token = (String) ServletUtil.getRequestAttribute(PROPERTIES.getSecurityName());
        }

        if ($.isEmpty(token)) {
            // 尝试从 header 头中获取
            token = ServletUtil.getHeader(PROPERTIES.getSecurityName());
        }

        if ($.isEmpty(token)) {
            // 尝试从 cookie 中读取
            SecurityProperties.CookieProperties cookieProperties = PROPERTIES.getCookieConfig();
            token = ServletUtil.getCookieValue(cookieProperties.getCookieName());
        }

        // 都没有拿到则认为没有认证登录
        if ($.isEmpty(token)) {
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
     * 裁剪 token 前缀
     *
     * @param token 令牌
     * @return {@link String }
     */
    @Nullable
    private static String cutPrefixToken(String token) {
        if ($.isEmpty(token)) {
            return null;
        }
        return token.replaceAll(SecurityConstant.AUTHORIZATION_PREFIX, "");
    }

    /**
     * 检查 token 信息
     */
    default void checkToken() {
        SecuritySession session = getCurrentSecuritySession();

        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("当前 token 已失效");
        }

        if (SecurityConstant.TOKEN_STATE_KICKED_OFFLINE.equals(session.getCurrentSecurityToken().getState())) {
            throw new UnauthorizedException("当前 token 已被踢下线");
        }

        if (SecurityConstant.TOKEN_STATE_REPLACE_OFFLINE.equals(session.getCurrentSecurityToken().getState())) {
            throw new UnauthorizedException("当前 token 已被顶下线");
        }

        if (SecurityConstant.TOKEN_STATE_BANNED.equals(session.getCurrentSecurityToken().getState())) {
            throw new UnauthorizedException("当前 token 已被封禁");
        }

        // 判断 token 续约操作
        SecurityToken securityToken = session.getCurrentSecurityToken();
        if (!SecurityConstant.NON_EXPIRING.equals(securityToken.getActivityTimeout())) {
            Long activityTimeout = tokenActivityTimeout(securityToken.getToken());
            if (null == activityTimeout || activityTimeout <= 0) {
                throw new UnauthorizedException("当前 token 已超时");
            }

            if (PROPERTIES.getAutoRenewal()) {
                // 续约操作
                renewalToken(securityToken.getToken());
            }
        }
    }

    /**
     * 检查 token 信息
     *
     * @param token 用户 token
     */
    default void checkToken(String token) {
        SecuritySession session = getSecuritySessionByToken(token);

        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("指定 token 已失效");
        }

        if (SecurityConstant.TOKEN_STATE_KICKED_OFFLINE.equals(session.getCurrentSecurityToken().getState())) {
            throw new UnauthorizedException("指定 token 已被踢下线");
        }

        if (SecurityConstant.TOKEN_STATE_REPLACE_OFFLINE.equals(session.getCurrentSecurityToken().getState())) {
            throw new UnauthorizedException("指定 token 已被顶下线");
        }

        if (SecurityConstant.TOKEN_STATE_BANNED.equals(session.getCurrentSecurityToken().getState())) {
            throw new UnauthorizedException("指定 token 已被封禁");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        if (!SecurityConstant.NON_EXPIRING.equals(securityToken.getActivityTimeout())) {
            Long activityTimeout = tokenActivityTimeout(securityToken.getToken());
            if (null == activityTimeout || activityTimeout <= 0) {
                throw new UnauthorizedException("指定 token 已超时");
            }
        }
    }

    /**
     * 踢下线操作
     */
    default void kickOut() {
        kickOut(null);
    }

    /**
     * 踢下线操作
     *
     * @param token 用户 token
     */
    default void kickOut(String token) {
        SecuritySession session = $.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被踢下线的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), SecurityConstant.TOKEN_STATE_KICKED_OFFLINE);
        session.flushSessionStorage();
        SecurityEventPublishManager.doKickOut(session.getLoginId(), securityToken.getToken(),
                securityToken.getDeviceType());
    }

    /**
     * 顶下线操作
     */
    default void replaceOut() {
        replaceOut(null);
    }

    /**
     * 顶下线操作
     *
     * @param token 用户 token
     */
    default void replaceOut(String token) {
        SecuritySession session = $.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被顶下线的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), SecurityConstant.TOKEN_STATE_REPLACE_OFFLINE);
        session.flushSessionStorage();
        SecurityEventPublishManager.doReplaceOut(session.getLoginId(), securityToken.getToken(),
                securityToken.getDeviceType());
    }

    /**
     * 续约 token
     */
    default void renewalToken() {
        renewalToken(null);
    }

    /**
     * 续约 token
     *
     * @param token 用户 token
     */
    default void renewalToken(String token) {
        SecuritySession session = $.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            return;
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        // 判断上次续约与本次的间隔
        Long autoRenewalIntervalTime = PROPERTIES.getAutoRenewalIntervalTime();
        LocalDateTime activityTime = this.tokenLastActivityTime(securityToken.getToken());
        if (Objects.nonNull(activityTime)
            && activityTime.plusSeconds(autoRenewalIntervalTime).isBefore(LocalDateTime.now())) {
            session.renewalToken(securityToken.getToken());
            // 续约成功通知
            SecurityEventPublishManager.doRenewal(session.getLoginId(), securityToken.getToken(),
                    securityToken.getDeviceType());
        }
    }

    /**
     * 封禁 token
     */
    default void bannedToken() {
        bannedToken(null);
    }

    /**
     * 封禁 token
     *
     * @param token 用户 token
     */
    default void bannedToken(String token) {
        SecuritySession session = $.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被封禁的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), SecurityConstant.TOKEN_STATE_BANNED);
        session.flushSessionStorage();
        SecurityEventPublishManager.doBanned(session.getLoginId(), securityToken.getToken(),
                securityToken.getDeviceType());
    }

    /**
     * 解封 token
     */
    default void unsealToken() {
        unsealToken(null);
    }

    /**
     * 解封 token
     *
     * @param token 用户 token
     */
    default void unsealToken(String token) {
        SecuritySession session = $.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被解封的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        session.updateTokenInfoState(securityToken.getToken(), SecurityConstant.TOKEN_STATE_NORMAL);
        session.flushSessionStorage();
        SecurityEventPublishManager.doUnseal(session.getLoginId(), securityToken.getToken(),
                securityToken.getDeviceType());
    }

    /**
     * 删除 token
     *
     * @param token 用户 token
     */
    default void removeToken(String token) {
        SecuritySession session = getSecuritySessionByToken(token);
        if (Objects.isNull(session) || Objects.isNull(session.getCurrentSecurityToken())) {
            throw new UnauthorizedException("需要被删除的 token 无效");
        }
        SecurityToken securityToken = session.getCurrentSecurityToken();
        String deviceType = securityToken.getDeviceType();
        session.removeTokenInfo(securityToken.getToken());
        session.flushSessionStorage();
        if (REPOSITORY.removeTokenByTokenValue(securityToken.getToken())) {
            // token 被删除通知
            SecurityEventPublishManager.doRemove(session.getLoginId(), securityToken.getToken(), deviceType);
        }
    }

    /**
     * 退出操作
     */
    default void loginOut() {
        loginOut(null);
    }

    /**
     * 退出操作
     *
     * @param token 用户 token
     */
    default void loginOut(String token) {
        SecuritySession session = $.isEmpty(token) ? getCurrentSecuritySession() : getSecuritySessionByToken(token);
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
        ServletUtil.removeRequestAttribute(PROPERTIES.getSecurityName());

        // 清理 cookie
        if (Boolean.TRUE.equals(PROPERTIES.getEnableCookie())) {
            SecurityProperties.CookieProperties cookieProperties = PROPERTIES.getCookieConfig();
            ServletUtil.addCookie(cookieProperties.getCookieName(), null, 0);
            ServletUtil.addCookie(SecurityConstant.SECURITY_SESSION_ID, null, 0);
        }

        if (REPOSITORY.removeTokenByTokenValue(securityToken.getToken())) {
            SecurityEventPublishManager.doLoginOut(session.getLoginId(), securityToken.getToken(), deviceType);
        }
    }

    /**
     * 查询 SecuritySession 列表
     *
     * @param tokenValue token 值，支持模糊匹配
     * @param sortedDesc 是否降序
     * @return SecurityPagination
     */
    default List<SecuritySession> querySecuritySessionList(String tokenValue, Boolean sortedDesc) {
        List<String> list = REPOSITORY.queryTokenList(tokenValue, sortedDesc);
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
     * @return List<String>
     */
    default List<String> queryTokenValueList(String tokenValue, Boolean sortedDesc) {
        return REPOSITORY.queryTokenList(tokenValue, sortedDesc);
    }

    /**
     * 获取 session 超时时间
     *
     * @param loginId 登录 Id
     * @return 时长秒 -1 表示永久有效
     */
    default Long sessionTimeout(String loginId) {
        return REPOSITORY.getSessionTimeoutByLoginId(loginId);
    }

    /**
     * 获取 token 超时时间
     *
     * @param token tokenValue
     * @return 时长秒 -1 表示永久有效
     */
    default Long tokenTimeout(String token) {
        return REPOSITORY.getTokenTimeOutByTokenValue(token);
    }

    /**
     * 获取 token 临时超时时间
     *
     * @param token tokenValue
     * @return 时长秒 -1 表示永久有效
     */
    default Long tokenActivityTimeout(String token) {
        return REPOSITORY.getTokenActivityTimeOutByTokenValue(token);
    }

    /**
     * 获取 token 的最新续约时间
     *
     * @param token tokenValue
     * @return 续约时间
     */
    default LocalDateTime tokenLastActivityTime(String token) {
        String activityTime = REPOSITORY.getActivityTimeByTokenValue(token);
        if ($.isEmpty(activityTime)) {
            return null;
        }
        return $.parseDateTime(activityTime);
    }

    /**
     * 当前用户是否有指定角色
     *
     * @param roleCode 角色码
     * @return true 有 false 没有
     */
    default boolean hasRole(String roleCode) {
        return PermissionUtil.hasRole(roleCode);
    }

    /**
     * 当前用户是否有指定角色
     *
     * @param roleCode 角色码
     * @param logical  条件
     * @return true 有 false 没有
     */
    default Boolean hasRole(String[] roleCode, Logical logical) {
        return PermissionUtil.hasMultiPermValid(List.of(roleCode), logical, PermissionUtil.getRoles());
    }

    /**
     * 当前用户是否有指定权限码
     *
     * @param permissionCode 权限码
     * @return true 有 false 没有
     */
    default Boolean hasPermission(String permissionCode) {
        return PermissionUtil.hasPermission(permissionCode);
    }

    /**
     * 当前用户是否有指定权限码
     *
     * @param permissionCode 权限码
     * @param logical        条件
     * @return true 有 false 没有
     */
    default Boolean hasPermission(String[] permissionCode, Logical logical) {
        return PermissionUtil.hasMultiPermValid(List.of(permissionCode), logical, PermissionUtil.getPermissions());
    }

    /**
     * 是否已登录
     *
     * @return true 登录 false 未登录
     */
    default Boolean isLogin() {
        try {
            checkToken();
        } catch (UnauthorizedException e) {
            return false;
        }
        return true;
    }

    /**
     * 身份临时切换
     *
     * @param loginId            登录 Id
     * @param identitySwitchFunc 执行函数
     */
    default void identityTempSwitching(String loginId, IdentitySwitchFunction identitySwitchFunc) {
        // 根据登录 Id 查询
        SecuritySession session = getSecuritySessionByLoginId(loginId);
        String token = null;
        if (Objects.nonNull(session)) {
            Optional<SecurityToken> optional = session.getTokenInfoList()
                    .stream()
                    .filter(item -> item.getState().equals(SecurityConstant.TOKEN_STATE_NORMAL))
                    .findFirst();
            if (optional.isPresent()) {
                token = optional.get().getToken();
            }
        }
        if ($.isEmpty(token)) {
            token = this.createLoginByLoginId(loginId, new SecurityLoginParams());
        }
        try {
            ServletUtil.setRequestAttribute(SecurityConstant.SECURITY_CUSTOM_IDENTITY_TOKEN, token);
            identitySwitchFunc.run();
        } finally {
            ServletUtil.removeRequestAttribute(SecurityConstant.SECURITY_CUSTOM_IDENTITY_TOKEN);
        }
    }

    /**
     * token 数量统计
     *
     * @return 总数
     */
    default Long getTokenCount() {
        List<String> list = REPOSITORY.queryTokenList(null, true);
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
    private void setRequestInfo(SecuritySession securitySession) {
        // 清空全局存储的自定义 token，防止登录时用户传入导致登录异常
        ServletUtil.removeRequestAttribute(SecurityConstant.SECURITY_CUSTOM_IDENTITY_TOKEN);
        // 将 token 设置到请求参数中
        ServletUtil.setRequestAttribute(PROPERTIES.getSecurityName(),
                appendTokenPrefix(securitySession.getCurrentSecurityToken().getToken()));
    }

    /**
     * 拼接 token 前缀
     */
    private static String appendTokenPrefix(String token) {
        return SecurityConstant.AUTHORIZATION_PREFIX + token;
    }

    /**
     * 设置响应信息
     *
     * @param securitySession SecuritySession
     */
    private void setResponseInfo(SecuritySession securitySession) {
        // 设置 header
        ServletUtil.setHeader(PROPERTIES.getSecurityName(),
                appendTokenPrefix(securitySession.getCurrentSecurityToken().getToken()));
        ServletUtil.addHeader("Access-Control-Expose-Headers", PROPERTIES.getSecurityName());

        // 设置 cookie
        if (Boolean.TRUE.equals(PROPERTIES.getEnableCookie())) {
            SecurityProperties.CookieProperties cookieProperties = PROPERTIES.getCookieConfig();
            // 将 security token value 写入 cookie
            ServletUtil.addCookie(cookieProperties.getCookieName(),
                    securitySession.getCurrentSecurityToken().getToken(),
                    securitySession.getCurrentSecurityToken().getTimeout().intValue());
            // 将 security session id 写入 cookie
            ServletUtil.addCookie(SecurityConstant.SECURITY_SESSION_ID, securitySession.getSecuritySessionId(),
                    securitySession.getCurrentSecurityToken().getTimeout().intValue());
        }
    }

    /**
     * 检查并设置登录信息
     *
     * @param loginId 登录 id
     * @param model   登录参数
     * @return SecuritySession {@link SecuritySession}
     */
    private SecuritySession checkAndSetSecuritySession(String loginId, SecurityLoginParams model) {
        // 判断是否超过最大颁发 token 数
        if (!SecurityConstant.NON_LIMIT.equals(PROPERTIES.getIssueTokenMaxLimit())) {
            long tokenCount = this.getTokenCount();
            if (PROPERTIES.getIssueTokenMaxLimit() >= tokenCount) {
                throw new UnauthorizedException("颁发 token 已超过最大限制数");
            }
        }
        SecurityToken securityToken = model.convert(loginId);
        SecuritySession session = REPOSITORY.getSecuritySessionByLoginId(loginId);
        if (Objects.isNull(session)) {
            session = model.convert(loginId, securityToken);
            return session;
        }

        SecuritySession securitySession = session;
        if (!model.getMountData().isEmpty()) {
            securitySession.getMountData().putAll(model.getMountData());
        }
        securitySession.setCurrentSecurityToken(securityToken);

        List<SecurityToken> availableTokenInfoList = securitySession.getTokenInfoList()
                .stream()
                .filter(item -> SecurityConstant.TOKEN_STATE_NORMAL.equals(item.getState()))
                .toList();
        if (!availableTokenInfoList.isEmpty()) {
            // 最先登录的 token
            SecurityToken earliestToken = availableTokenInfoList.getFirst();
            // 验证登录设备类型数量
            if (!SecurityConstant.NON_LIMIT.equals(PROPERTIES.getMaxLoginDeviceTypeLimit())
                && availableTokenInfoList.stream()
                           .map(SecurityToken::getDeviceType)
                           .distinct()
                           .count() >= PROPERTIES.getMaxLoginDeviceTypeLimit()) {

                availableTokenInfoList.stream()
                        .filter(item -> item.getDeviceType().equals(earliestToken.getDeviceType()))
                        .forEach(tokenInfo -> {
                            // 已达到登录设备类型上限，顶掉最先登录设备的所有相同的设备类型
                            securitySession.updateTokenInfoState(tokenInfo.getToken(),
                                    SecurityConstant.TOKEN_STATE_REPLACE_OFFLINE);
                            SecurityEventPublishManager.doReplaceOut(loginId, tokenInfo.getToken(),
                                    tokenInfo.getDeviceType());
                        });
            }
            if (Boolean.TRUE.equals(PROPERTIES.getIsConcurrentLogin())) {
                // 允许并发，验证登录设备数量
                if (!SecurityConstant.NON_LIMIT.equals(PROPERTIES.getMaxLoginLimit())
                    && availableTokenInfoList.size() >= PROPERTIES.getMaxLoginLimit()) {
                    // 已达到登录上限，顶掉最先登录的设备
                    securitySession.updateTokenInfoState(earliestToken.getToken(),
                            SecurityConstant.TOKEN_STATE_REPLACE_OFFLINE);
                    SecurityEventPublishManager.doReplaceOut(loginId, earliestToken.getToken(),
                            earliestToken.getDeviceType());
                }
            } else {
                // 将旧的登录信息修改为被顶下线状态
                availableTokenInfoList.stream()
                        .filter(item -> item.getDeviceType()
                                .equals(securitySession.getCurrentSecurityToken().getDeviceType()))
                        .forEach(tokenInfo -> {
                            securitySession.updateTokenInfoState(tokenInfo.getToken(),
                                    SecurityConstant.TOKEN_STATE_REPLACE_OFFLINE);
                            SecurityEventPublishManager.doReplaceOut(securitySession.getLoginId(), tokenInfo.getToken(),
                                    tokenInfo.getDeviceType());
                        });
            }
        }

        securitySession.setUpdateTime($.formatDateTime(LocalDateTime.now()));
        securitySession.addTokenInfo(securitySession.getCurrentSecurityToken());
        return securitySession;
    }

}
