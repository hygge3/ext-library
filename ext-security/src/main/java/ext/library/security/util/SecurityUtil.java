package ext.library.security.util;

import ext.library.core.util.ServletUtil;
import ext.library.core.util.SpringUtil;
import ext.library.http.useragent.UserAgent;
import ext.library.http.useragent.UserAgentUtil;
import ext.library.security.domain.SecurityLoginParams;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.security.enums.Logical;
import ext.library.security.service.SecurityService;
import ext.library.tool.holder.Lazy;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证便捷操作工具
 */
public class SecurityUtil {

    /**
     * 获取 service
     * <p>
     * 返回 SecurityService
     */
    private static final Lazy<SecurityService> SERVICE = Lazy.of(() -> SpringUtil.getBean(SecurityService.class));

    /*
     * ***************************************操作相关方法**************************************
     * *
     */
    private static String getPlatform() {
        String ua = ServletUtil.getUA();
        UserAgent userAgent = UserAgentUtil.parse(ua);
        return userAgent.getPlatform().getName();
    }

    /**
     * 登录操作
     *
     * @param loginId 账号 ID
     */
    public static void doLogin(String loginId) {
        doLogin(loginId, new SecurityLoginParams().setDeviceType(getPlatform()));
    }

    /**
     * 登录操作
     *
     * @param loginId    账号 ID
     * @param loginModel 参数
     */
    public static void doLogin(String loginId, SecurityLoginParams loginModel) {
        SERVICE.get().doLogin(loginId, loginModel);
    }

    /**
     * 登录操作
     *
     * @param loginId 账号 ID
     * @param data    参数
     */
    public static void doLogin(String loginId, Object data) {
        SecurityLoginParams securityLoginParams = new SecurityLoginParams();
        securityLoginParams.setDeviceType(getPlatform());
        securityLoginParams.setAttributes(data);
        SERVICE.get().doLogin(loginId, securityLoginParams);
    }

    /**
     * 创建指定账号的登录信息
     *
     * @param loginId 登录 Id
     *
     * @return token
     */
    public static String createLoginByLoginId(String loginId) {
        return createLoginByLoginId(loginId, new SecurityLoginParams());
    }

    /**
     * 创建指定账号的登录信息
     *
     * @param loginId    登录 Id
     * @param loginModel 登录参数
     *
     * @return token
     */
    public static String createLoginByLoginId(String loginId, SecurityLoginParams loginModel) {
        return SERVICE.get().createLoginByLoginId(loginId, loginModel);
    }

    /**
     * 检查 token 信息
     */
    public static void checkToken() {
        SERVICE.get().checkToken();
    }

    /**
     * 续约 token
     */
    public static void renewalToken() {
        SERVICE.get().renewalToken();
    }

    /**
     * 退出操作
     */
    public static void loginOut() {
        SERVICE.get().loginOut();
    }

    /**
     * 指定 token 退出
     *
     * @param token token
     */
    public static void loginOut(String token) {
        SERVICE.get().loginOut(token);
    }

    /**
     * 指定 token 顶下线操作
     *
     * @param token 用户 token
     */
    public static void replaceToken(String token) {
        SERVICE.get().replaceOut(token);
    }

    /**
     * 指定 token 踢下线操作
     *
     * @param token 用户 token
     */
    public static void kickToken(String token) {
        SERVICE.get().kickOut(token);
    }

    /**
     * 指定 token 封禁操作
     *
     * @param token 用户 token
     */
    public static void bannedToken(String token) {
        SERVICE.get().bannedToken(token);
    }

    /**
     * 指定 token 解封操作
     *
     * @param token 用户 token
     */
    public static void unsealToken(String token) {
        SERVICE.get().unsealToken(token);
    }

    /**
     * 指定 token 移除操作
     *
     * @param token 用户 token
     */
    public static void removeToken(String token) {
        SERVICE.get().removeToken(token);
    }

    /*
     * ***************************************查询相关方法**************************************
     * *
     */

    /**
     * 获取当前登录的 SecuritySession
     *
     * @return {@link SecuritySession}
     */
    public static SecuritySession getCurrentSecuritySession() {
        return SERVICE.get().getCurrentSecuritySession();
    }

    /**
     * 获取当前的 tokenValue
     *
     * @return token
     */
    public static String getCurrentTokenValue() {
        return getCurrentSecuritySession().getCurrentSecurityToken().getToken();
    }

    /**
     * 获取当前的 token 信息
     *
     * @return token
     */
    public static SecurityToken getCurrentToken() {
        return getCurrentSecuritySession().getCurrentSecurityToken();
    }

    /**
     * 获取当前的登录 Id
     *
     * @return String
     */
    public static String getCurrentLoginId() {
        return getCurrentSecuritySession().getLoginId();
    }

    /**
     * 获取 session 超时时间
     *
     * @param token token
     *
     * @return 时长秒 -1 表示永久有效
     */
    public static Long getSessionTimeout(String token) {
        return SERVICE.get().sessionTimeout(token);
    }

    /**
     * 获取 token 超时时间
     *
     * @param token token
     *
     * @return 时长秒 -1 表示永久有效
     */
    public static Long getTokenTimeout(String token) {
        return SERVICE.get().tokenTimeout(token);
    }

    /**
     * 获取指定 token 活跃超时时间
     *
     * @param token token 值
     *
     * @return 时长秒 -1 表示永久有效
     */
    public static Long getTokenActivityTimeout(String token) {
        return SERVICE.get().tokenActivityTimeout(token);
    }

    /**
     * 获取指定 token 最新续约时间
     *
     * @param token token 值
     *
     * @return 续约时间
     */
    public static LocalDateTime getTokenLastActivityTime(String token) {
        return SERVICE.get().tokenLastActivityTime(token);
    }

    /**
     * 是否已登录
     *
     * @return true 登录 false 未登录
     */
    public static Boolean isLogin() {
        return SERVICE.get().isLogin();
    }

    /**
     * 当前用户是否有指定角色
     *
     * @param roleCode 角色码
     *
     * @return true 有 false 没有
     */
    public static Boolean hasRole(String roleCode) {
        return SERVICE.get().hasRole(roleCode);
    }

    /**
     * 当前用户是否有指定角色
     *
     * @param roleCode 角色码
     * @param logical  条件
     *
     * @return true 有 false 没有
     */
    public static Boolean hasRole(String[] roleCode, Logical logical) {
        return SERVICE.get().hasRole(roleCode, logical);
    }

    /**
     * 当前用户是否有指定权限码
     *
     * @param permissionCode 权限码
     *
     * @return true 有 false 没有
     */
    public static Boolean hasPermission(String permissionCode) {
        return SERVICE.get().hasPermission(permissionCode);
    }

    /**
     * 当前用户是否有指定权限码
     *
     * @param permissionCode 权限码
     * @param logical        条件
     *
     * @return true 有 false 没有
     */
    public static Boolean hasPermission(String[] permissionCode, Logical logical) {
        return SERVICE.get().hasPermission(permissionCode, logical);
    }

    /**
     * 查询 SecuritySession 列表
     *
     * @param tokenValue token
     * @param sortedDesc 是否降序
     *
     * @return List<SecuritySession>
     */
    public static List<SecuritySession> querySecuritySessionList(String tokenValue, boolean sortedDesc) {
        return SERVICE.get().querySecuritySessionList(tokenValue, sortedDesc);
    }

}