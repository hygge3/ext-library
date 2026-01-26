package ext.library.security.domain;

import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.enums.TokenState;
import ext.library.security.properties.SecurityProperties;
import ext.library.tool.util.IdUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 登录参数
 * </p>
 */

public class SecurityLoginParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * session 挂载数据
     */
    private final Map<String, Object> mountData = new HashMap<>();
    /**
     * token 挂载数据
     */
    private final Map<String, Object> tokenMountData = new HashMap<>();
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
     * 获取 session 挂载数据
     *
     * @return session 挂载数据
     */
    public Map<String, Object> getMountData() {
        return mountData;
    }

    /**
     * 获取 token 挂载数据
     *
     * @return token 挂载数据
     */
    public Map<String, Object> getTokenMountData() {
        return tokenMountData;
    }

    /**
     * 获取超时时间
     *
     * @return 超时时间
     */
    public Long getTimeout() {
        return timeout;
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
     * 获取活跃超时时间
     *
     * @return 活跃超时时间
     */
    public Long getActivityTimeout() {
        return activityTimeout;
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

    /**
     * 转换为 SecurityToken
     *
     * @param loginId 登录 ID
     * @return SecurityToken
     */
    public SecurityToken convert(String loginId) {
        SecurityProperties properties = SpringUtil.getBean(SecurityProperties.class);
        LocalDateTime now = LocalDateTime.now();

        SecurityToken securityToken = new SecurityToken();
        securityToken.setToken(IdUtil.getUUIDv7());
        securityToken.setLoginId(loginId);
        securityToken.setDeviceType(Objects.toString(this.getDeviceType(), SecurityConstant.UNKNOWN));
        securityToken.setTimeout(this.getTimeout() != null ? this.getTimeout() : properties.getTimeoutSeconds());
        securityToken.setActivityTime(now);
        securityToken.setActivityTimeout(this.getActivityTimeout() != null ? this.getActivityTimeout() : properties.getActivityTimeoutSeconds());
        securityToken.setState(TokenState.NORMAL);
        securityToken.setCreateTime(now);
        securityToken.getTokenMountData().putAll(this.getTokenMountData());

        return securityToken;
    }

    /**
     * 转换为 SecuritySession
     *
     * @param loginId       登录 ID
     * @param securityToken SecurityToken，若为 null 则自动创建
     * @return SecuritySession
     */
    public SecuritySession convert(String loginId, SecurityToken securityToken) {
        SecurityProperties properties = SpringUtil.getBean(SecurityProperties.class);
        SecurityToken token = securityToken != null ? securityToken : this.convert(loginId);

        SecuritySession session = new SecuritySession(true);
        session.setLoginId(loginId);
        session.getMountData().putAll(this.getMountData());
        session.setTimeout(this.getTimeout() != null ? this.getTimeout() : properties.getTimeoutSeconds());
        session.setCurrentSecurityToken(token);
        session.setVersion(0L);
        session.addTokenInfo(token);

        return session;
    }
}
