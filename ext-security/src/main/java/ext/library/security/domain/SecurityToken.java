package ext.library.security.domain;

import ext.library.json.util.JsonUtil;
import ext.library.security.enums.TokenState;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Security Token 信息
 */
public class SecurityToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Token 挂载数据
     */
    private final Map<String, Object> tokenMountData = new HashMap<>();

    /**
     * Token 值
     */
    private String token;

    /**
     * 登录 ID
     */
    private String loginId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 过期时间（秒）
     */
    private Long timeout;

    /**
     * 活跃时间
     */
    private LocalDateTime activityTime;

    /**
     * 活跃超时时间（秒）
     */
    private Long activityTimeout;

    /**
     * 状态
     */
    private TokenState state;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public SecurityToken() {
    }

    public SecurityToken(String token, String loginId, String deviceType, Long timeout,
                         LocalDateTime activityTime, Long activityTimeout, TokenState state,
                         LocalDateTime createTime, LocalDateTime updateTime) {
        this.token = token;
        this.loginId = loginId;
        this.deviceType = deviceType;
        this.timeout = timeout;
        this.activityTime = activityTime;
        this.activityTimeout = activityTimeout;
        this.state = state;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // ==================== Getter/Setter ====================

    public Map<String, Object> getTokenMountData() {
        return tokenMountData;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public LocalDateTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }

    public Long getActivityTimeout() {
        return activityTimeout;
    }

    public void setActivityTimeout(Long activityTimeout) {
        this.activityTimeout = activityTimeout;
    }

    public TokenState getState() {
        return state;
    }

    public void setState(TokenState state) {
        this.state = state;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    // ==================== 属性操作 ====================

    /**
     * 获取存储属性
     *
     * @param key 键
     *
     * @return 值
     */
    public Object getAttribute(String key) {
        return tokenMountData.get(key);
    }

    /**
     * 获取存储属性并转换类型
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   泛型
     *
     * @return 转换后的对象
     */
    public <T> T getAttribute(String key, Class<T> clazz) {
        Object obj = getAttribute(key);
        return JsonUtil.readObj(JsonUtil.toJson(obj), clazz);
    }
}
