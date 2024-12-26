package ext.library.security.listener;

import ext.library.security.domain.SecurityLoginParams;
import lombok.experimental.UtilityClass;

/**
 * <p>
 * 事件发布管理
 * </p>
 */
@UtilityClass
public class SecurityEventPublishManager {

    /**
     * 登录接口操作触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param loginModel 登录参数
     */
    public void doLogin(String loginId, String token, SecurityLoginParams loginModel) {
        SecurityListenerManager.getListener().forEach(item -> item.doLogin(loginId, token, loginModel));
    }

    /**
     * 被踢下线操作触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doKickOut(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doKickOut(loginId, token, deviceType));
    }

    /**
     * 被顶下线操作触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doReplaceOut(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doReplaceOut(loginId, token, deviceType));
    }

    /**
     * 被封禁触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doBanned(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doBanned(loginId, token, deviceType));
    }

    /**
     * 解封触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doUnseal(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doUnseal(loginId, token, deviceType));
    }

    /**
     * 续约触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doRenewal(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doRenewal(loginId, token, deviceType));
    }

    /**
     * 移除触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doRemove(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doRemove(loginId, token, deviceType));
    }

    /**
     * 退出触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    public void doLoginOut(String loginId, String token, String deviceType) {
        SecurityListenerManager.getListener().forEach(item -> item.doLoginOut(loginId, token, deviceType));
    }

    /**
     * 创建 securitySession
     *
     * @param securitySessionId session id
     */
    public void doCreatedSecuritySession(String securitySessionId) {
        SecurityListenerManager.getListener().forEach(item -> item.doCreatedSecuritySession(securitySessionId));
    }

    /**
     * 销毁 securitySession
     *
     * @param securitySessionId session id
     */
    public void doDestroySecuritySession(String securitySessionId) {
        SecurityListenerManager.getListener().forEach(item -> item.doDestroySecuritySession(securitySessionId));
    }

}
