package ext.library.security.domain;

import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.enums.TokenState;
import ext.library.security.listener.SecurityEventPublishManager;
import ext.library.security.repository.SecurityRepository;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.IdUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Security Session 信息
 */
public class SecuritySession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 挂载数据
     */
    private final Map<String, Object> mountData = new HashMap<>();

    /**
     * Session ID
     */
    private String securitySessionId;

    /**
     * 登录 ID
     */
    private String loginId;

    /**
     * 过期时间（秒）
     */
    private Long timeout;

    /**
     * 当前 SecurityToken
     */
    private SecurityToken currentSecurityToken;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * Token 列表
     */
    private List<SecurityToken> tokenInfoList = new ArrayList<>();

    /**
     * 版本号
     */
    private Long version;

    public SecuritySession() {
    }

    public SecuritySession(boolean isCreate) {
        if (isCreate) {
            this.createSecuritySession();
        }
    }

    public SecuritySession(String securitySessionId, String loginId, Long timeout,
                           SecurityToken currentSecurityToken, LocalDateTime createTime,
                           LocalDateTime updateTime, List<SecurityToken> tokenInfoList, Long version) {
        this.securitySessionId = securitySessionId;
        this.loginId = loginId;
        this.timeout = timeout;
        this.currentSecurityToken = currentSecurityToken;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.tokenInfoList = tokenInfoList;
        this.version = version;
    }

    // ==================== Getter/Setter ====================

    public Map<String, Object> getMountData() {
        return mountData;
    }

    public String getSecuritySessionId() {
        return securitySessionId;
    }

    public void setSecuritySessionId(String securitySessionId) {
        this.securitySessionId = securitySessionId;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public SecurityToken getCurrentSecurityToken() {
        return currentSecurityToken;
    }

    public void setCurrentSecurityToken(SecurityToken currentSecurityToken) {
        this.currentSecurityToken = currentSecurityToken;
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

    public List<SecurityToken> getTokenInfoList() {
        return tokenInfoList;
    }

    public void setTokenInfoList(List<SecurityToken> tokenInfoList) {
        this.tokenInfoList = tokenInfoList;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // ==================== Session 操作 ====================

    /**
     * 创建 SecuritySession
     */
    public void createSecuritySession() {
        this.securitySessionId = IdUtil.getUUIDv7();
        this.createTime = LocalDateTime.now();
        SecurityEventPublishManager.doCreatedSecuritySession(this.securitySessionId);
    }

    /**
     * 销毁 SecuritySession
     */
    public void destroySecuritySession() {
        if (Objects.isNull(this.loginId)) {
            return;
        }
        SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);
        if (repository.removeSecuritySessionByLoginId(this.loginId)) {
            SecurityEventPublishManager.doDestroySecuritySession(this.securitySessionId);
        }
    }

    // ==================== Token 操作 ====================

    /**
     * 更新 Token 信息
     *
     * @param tokenInfo Token 信息
     * @return this
     */
    public SecuritySession updateTokenInfo(SecurityToken tokenInfo) {
        for (SecurityToken info : this.tokenInfoList) {
            if (info.getToken().equals(tokenInfo.getToken())) {
                if (tokenInfo.getState() != null) {
                    info.setState(tokenInfo.getState());
                }
                if (tokenInfo.getDeviceType() != null) {
                    info.setDeviceType(tokenInfo.getDeviceType());
                }
                if (tokenInfo.getTimeout() != null) {
                    info.setTimeout(tokenInfo.getTimeout());
                }
                if (tokenInfo.getActivityTimeout() != null) {
                    info.setActivityTimeout(tokenInfo.getActivityTimeout());
                }
                info.setUpdateTime(LocalDateTime.now());
                break;
            }
        }
        return this;
    }

    /**
     * 更新 Token 状态
     *
     * @param token Token 值
     * @param state 状态
     * @return this
     */
    public SecuritySession updateTokenInfoState(String token, TokenState state) {
        SecurityToken tokenInfo = new SecurityToken();
        tokenInfo.setToken(token);
        tokenInfo.setState(state);
        return this.updateTokenInfo(tokenInfo);
    }

    /**
     * Token 续约
     *
     * @param token Token 值
     * @return this
     */
    public SecuritySession renewalToken(String token) {
        SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);
        if (repository.renewalTokenByTokenValue(token)) {
            Logs.debug(EmojiSymbol.SECURITY, "续约成功：{}", token);
        }
        return this;
    }

    /**
     * 新增 Token 信息
     *
     * @param tokenInfo Token 信息
     * @return this
     */
    public SecuritySession addTokenInfo(SecurityToken tokenInfo) {
        this.tokenInfoList.add(tokenInfo);
        return this;
    }

    /**
     * 删除 Token 信息
     *
     * @param token Token 值
     * @return this
     */
    public SecuritySession removeTokenInfo(String token) {
        this.tokenInfoList.remove(getTokenInfoByToken(token));
        return this;
    }

    /**
     * 根据 Token 值获取 Token 信息
     *
     * @param token Token 值
     * @return SecurityToken
     */
    public SecurityToken getTokenInfoByToken(String token) {
        return tokenInfoList.stream()
                .filter(item -> item.getToken().equals(token))
                .findFirst()
                .orElse(null);
    }

    // ==================== 属性操作 ====================

    /**
     * 设置属性
     *
     * @param key   键
     * @param value 值
     * @return this
     */
    public SecuritySession setAttribute(String key, Object value) {
        mountData.put(key, value);
        flushSessionStorage();
        return fetchData();
    }

    /**
     * 获取属性
     *
     * @param key 键
     * @return 值
     */
    public Object getAttribute(String key) {
        return mountData.get(key);
    }

    /**
     * 获取所有属性并转换类型
     *
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 转换后的对象
     */
    public <T> T getAttributes(Class<T> clazz) {
        return JsonUtil.readObj(JsonUtil.toJson(mountData), clazz);
    }

    /**
     * 获取属性并转换类型
     *
     * @param key   键
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 转换后的对象
     */
    public <T> T getAttribute(String key, Class<T> clazz) {
        Object obj = getAttribute(key);
        return JsonUtil.readObj(JsonUtil.toJson(obj), clazz);
    }

    /**
     * 重新获取数据
     *
     * @return SecuritySession
     */
    public SecuritySession fetchData() {
        if (Objects.isNull(this.loginId)) {
            return null;
        }
        SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);
        return repository.getSecuritySessionByLoginId(this.loginId);
    }

    /**
     * 刷新 Session 存储
     */
    public void flushSessionStorage() {
        SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);

        // 清理无效 Token
        List<SecurityToken> invalidTokenList = new ArrayList<>();
        if (!this.getTokenInfoList().isEmpty()) {
            this.getTokenInfoList().forEach(securityToken -> {
                // 非正常状态且超过阈值时间的 Token 视为无效
                if (securityToken.getUpdateTime() != null
                        && securityToken.getState() != TokenState.NORMAL
                        && securityToken.getUpdateTime()
                        .plusHours(SecurityConstant.INVALID_TOKEN_CLEANUP_HOURS)
                        .isBefore(LocalDateTime.now())) {
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

        // 移除无效 Token
        invalidTokenList.forEach(item -> this.removeTokenInfo(item.getToken()));

        // 保存 Session
        boolean result = repository.saveSecuritySession(this);
        if (!result) {
            throw new ExtException(EmojiSymbol.SECURITY, "保存 Session 认证数据失败");
        }

        // 清理无效 Token 存储
        invalidTokenList.forEach(item -> {
            if (repository.removeTokenByTokenValue(item.getToken())) {
                SecurityEventPublishManager.doRemove(this.getLoginId(), item.getToken(), item.getDeviceType());
            }
        });
    }
}
