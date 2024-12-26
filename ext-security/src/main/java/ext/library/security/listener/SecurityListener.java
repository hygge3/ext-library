package ext.library.security.listener;

import java.util.Objects;

import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecurityLoginParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * 监听接口
 * </p>
 */

public interface SecurityListener {

    Logger log = LoggerFactory.getLogger(SecurityListener.class);

    /**
     * 登录触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param loginModel 登录参数
     */
    default void doLogin(String loginId, String token, SecurityLoginParams loginModel) {
        log.info("[ℹ️] 登录成功，account:{},token:{},deviceType:{}", loginId, token,
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
        log.info("[ℹ️] 被踢下线，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 被顶下线触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doReplaceOut(String loginId, String token, String deviceType) {
        log.info("[ℹ️] 被顶下线，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 被封禁触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doBanned(String loginId, String token, String deviceType) {
        log.info("[ℹ️] 封禁，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 解封触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doUnseal(String loginId, String token, String deviceType) {
        log.info("[ℹ️] 解封，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 续约触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doRenewal(String loginId, String token, String deviceType) {
        log.info("续约，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 移除触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doRemove(String loginId, String token, String deviceType) {
        log.info("[ℹ️] 删除，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 退出触发
     *
     * @param loginId    登录 Id
     * @param token      token 值
     * @param deviceType 设备类型
     */
    default void doLoginOut(String loginId, String token, String deviceType) {
        log.info("[ℹ️] 退出，account:{},token:{},deviceType:{}", loginId, token, deviceType);
    }

    /**
     * 创建 securitySession
     *
     * @param securitySessionId session id
     */
    default void doCreatedSecuritySession(String securitySessionId) {
        log.info("[ℹ️] 创建 Session，securitySessionId:{}", securitySessionId);
    }

    /**
     * 销毁 securitySession
     *
     * @param securitySessionId session id
     */
    default void doDestroySecuritySession(String securitySessionId) {
        log.info("[ℹ️] 销毁 Session，securitySessionId:{}", securitySessionId);
    }

}
