package ext.library.security.listener;

import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecurityLoginParams;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.util.Objects;

/**
 * <p>
 * 监听接口
 * </p>
 */

public interface SecurityListener {

    /**
     * 登录触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param loginModel 登录参数
     */
    default void doLogin(String loginId, String token, SecurityLoginParams loginModel) {
        Logs.info(EmojiSymbol.SECURITY, "登录成功，account:{},token:{},deviceType:{}", loginId, token,
                Objects.nonNull(loginModel) ? loginModel.getDeviceType() : SecurityConstant.UNKNOWN);
    }

    /**
     * 被踢下线触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doKickOut(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "被踢下线，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 被顶下线触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doReplaceOut(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "被顶下线，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 被封禁触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doBanned(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "封禁，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 解封触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doUnseal(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "解封，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 续约触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doRenewal(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "续约，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 移除触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doRemove(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "删除，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 退出触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doLogout(String loginId, String token, String deviceType) {
        Logs.info(EmojiSymbol.SECURITY, "退出，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 创建 securitySession
     *
     * @param securitySessionId session id
     */
    default void doCreatedSecuritySession(String securitySessionId) {
        Logs.info(EmojiSymbol.SECURITY, "创建 Session，securitySessionId:{}", securitySessionId);
    }

    /**
     * 销毁 securitySession
     *
     * @param securitySessionId session id
     */
    default void doDestroySecuritySession(String securitySessionId) {
        Logs.info(EmojiSymbol.SECURITY, "销毁 Session，securitySessionId:{}", securitySessionId);
    }

}
