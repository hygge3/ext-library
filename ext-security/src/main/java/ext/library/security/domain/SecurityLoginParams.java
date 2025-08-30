package ext.library.security.domain;

import com.google.common.collect.Maps;
import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.security.config.properties.SecurityProperties;
import ext.library.security.constants.SecurityConstant;
import ext.library.tool.constant.Holder;
import ext.library.tool.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 登录参数
 * </p>
 */
@Getter
@Setter
public class SecurityLoginParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * session 挂载数据
     */
    private final Map<String, Object> mountData = Maps.newHashMap();
    /**
     * token 挂载数据
     */
    private final Map<String, Object> tokenMountData = Maps.newHashMap();
    /**
     * 设备类型
     */
    private String deviceType;
    /**
     * 超时时间
     */
    private Long timeout;
    /**
     * 活跃超时时间
     */
    private Long activityTimeout;

    /**
     * 获取设备类型
     *
     * @return 设备类型
     */
    public String getDeviceType() {
        if (Objects.isNull(this.deviceType)) {
            this.deviceType = SecurityConstant.UNKNOWN;
        }
        return deviceType;
    }

    /**
     * 设置设备类型
     *
     * @param deviceType 设备类型
     *
     * @return SecurityLoginParams
     */
    public SecurityLoginParams setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * 设置超时时间
     *
     * @param timeout 超时时间
     *
     * @return SecurityLoginParams
     */
    public SecurityLoginParams setTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置活跃超时时间
     *
     * @param activityTimeout 活跃超时时间
     *
     * @return SecurityLoginParams
     */
    public SecurityLoginParams setActivityTimeout(Long activityTimeout) {
        this.activityTimeout = activityTimeout;
        return this;
    }

    /**
     * 设置存储信息
     *
     * @param key   key 值
     * @param value value 值
     *
     * @return SecuritySession
     */
    public SecurityLoginParams setAttribute(String key, Object value) {
        mountData.put(key, value);
        return this;
    }

    /**
     * 设置存储信息
     *
     * @param data 数据
     *
     * @return SecuritySession
     */
    public SecurityLoginParams setAttributes(Map<String, Object> data) {
        mountData.putAll(data);
        return this;
    }

    /**
     * 设置存储信息
     *
     * @param data 数据
     *
     * @return SecuritySession
     */
    public SecurityLoginParams setAttributes(Object data) {
        Map<String, Object> map = JsonUtil.readMap(JsonUtil.toJson(data));
        mountData.putAll(map);
        return this;
    }

    /**
     * 获取存储信息
     *
     * @param key key 值
     *
     * @return Object
     */
    public Object getAttribute(String key) {
        return mountData.get(key);
    }

    /**
     * 设置 token 存储信息
     *
     * @param key   key 值
     * @param value value 值
     *
     * @return SecuritySession
     */
    public SecurityLoginParams setTokenAttribute(String key, Object value) {
        tokenMountData.put(key, value);
        return this;
    }

    /**
     * 获取 token 存储信息
     *
     * @param key key 值
     *
     * @return Object
     */
    public Object getTokenAttribute(String key) {
        return tokenMountData.get(key);
    }

    public SecurityToken convert(String loginId) {
        SecurityProperties properties = SpringUtil.getBean(SecurityProperties.class);
        SecurityToken securityToken = SecurityToken.builder()
                .token(Holder.ULID.nextULID())
                .loginId(loginId)
                .deviceType(Objects.toString(this.getDeviceType(), SecurityConstant.UNKNOWN))
                .timeout(Objects.isNull(this.getTimeout()) ? properties.getTimeout() : this.getTimeout())
                .activityTime(DateUtil.format(LocalDateTime.now()))
                .activityTimeout(Objects.isNull(this.getActivityTimeout()) ? properties.getActivityTimeout()
                        : this.getActivityTimeout())
                .state(SecurityConstant.TOKEN_STATE_NORMAL)
                .createTime(DateUtil.format(LocalDateTime.now()))
                .build();
        // 设置 token 挂载数据
        securityToken.getTokenMountData().putAll(this.getTokenMountData());
        return securityToken;
    }

    public SecuritySession convert(String loginId, SecurityToken securityToken) {
        SecurityProperties properties = SpringUtil.getBean(SecurityProperties.class);
        SecurityToken token = Objects.isNull(securityToken) ? this.convert(loginId) : securityToken;
        SecuritySession session = new SecuritySession(true);
        session.setLoginId(loginId);
        session.getMountData().putAll(this.getMountData());
        session.setTimeout(Objects.isNull(this.getTimeout()) ? properties.getTimeout() : this.getTimeout());
        session.setCurrentSecurityToken(token);
        session.setVersion(0L);
        session.addTokenInfo(token);
        return session;
    }

}