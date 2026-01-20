package ext.library.security.domain;

import ext.library.core.util.SpringUtil;
import ext.library.json.util.JsonUtil;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.listener.SecurityEventPublishManager;
import ext.library.security.repository.SecurityRepository;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.DateUtil;
import ext.library.tool.util.IdUtil;
import ext.library.tool.util.StringUtil;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 认证 session 信息
 * </p>
 */
public class SecuritySession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 挂载数据
     */
    private final Map<String, Object> mountData = new HashMap<>();
    /**
     * sessionId
     */
    private String securitySessionId;
    /**
     * 登录 Id
     */
    private String loginId;
    /**
     * 过期时间 单位秒
     */
    private Long timeout;
    /**
     * 当前的 SecurityToken
     */
    private SecurityToken currentSecurityToken;
    /**
     * 创建时间 格式 yyyy-MM-dd HH:mm:ss
     */
    private String createTime;

    /**
     * 更新时间 格式 yyyy-MM-dd HH:mm:ss
     */
    private String updateTime;

    /**
     * 登录的 token 列表
     */
    private List<SecurityToken> tokenInfoList = new ArrayList<>();

    /**
     * 版本号
     */
    private Long version;

    public SecuritySession(boolean isCreate) {
        if (isCreate) {
            this.createdSecuritySession();
        }
    }

    public SecuritySession(String securitySessionId, String loginId, Long timeout, SecurityToken currentSecurityToken, String createTime, String updateTime, List<SecurityToken> tokenInfoList, Long version) {
        this.securitySessionId = securitySessionId;
        this.loginId = loginId;
        this.timeout = timeout;
        this.currentSecurityToken = currentSecurityToken;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.tokenInfoList = tokenInfoList;
        this.version = version;
    }

    public SecuritySession() {
    }

    /**
     * 获取挂载数据
     *
     * @return 挂载数据
     */
    public Map<String, Object> getMountData() {
        return mountData;
    }

    /**
     * 获取 sessionId
     *
     * @return sessionId
     */
    public String getSecuritySessionId() {
        return securitySessionId;
    }

    /**
     * 设置 sessionId
     *
     * @param securitySessionId sessionId
     */
    public void setSecuritySessionId(String securitySessionId) {
        this.securitySessionId = securitySessionId;
    }

    /**
     * 获取登录 Id
     *
     * @return 登录 Id
     */
    public String getLoginId() {
        return loginId;
    }

    /**
     * 设置登录 Id
     *
     * @param loginId 登录 Id
     */
    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    /**
     * 获取过期时间 单位秒
     *
     * @return 过期时间
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * 设置过期时间 单位秒
     *
     * @param timeout 过期时间
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * 获取当前的 SecurityToken
     *
     * @return 当前的 SecurityToken
     */
    public SecurityToken getCurrentSecurityToken() {
        return currentSecurityToken;
    }

    /**
     * 设置当前的 SecurityToken
     *
     * @param currentSecurityToken 当前的 SecurityToken
     */
    public void setCurrentSecurityToken(SecurityToken currentSecurityToken) {
        this.currentSecurityToken = currentSecurityToken;
    }

    /**
     * 获取创建时间 格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 创建时间
     */
    public String getCreateTime() {
        return createTime;
    }

    /**
     * 设置创建时间 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param createTime 创建时间
     */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取更新时间 格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 更新时间
     */
    public String getUpdateTime() {
        return updateTime;
    }

    /**
     * 设置更新时间 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param updateTime 更新时间
     */
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 获取登录的 token 列表
     *
     * @return 登录的 token 列表
     */
    public List<SecurityToken> getTokenInfoList() {
        return tokenInfoList;
    }

    /**
     * 设置登录的 token 列表
     *
     * @param tokenInfoList 登录的 token 列表
     */
    public void setTokenInfoList(List<SecurityToken> tokenInfoList) {
        this.tokenInfoList = tokenInfoList;
    }

    /**
     * 获取版本号
     *
     * @return 版本号
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 设置版本号
     *
     * @param version 版本号
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 创建 SecuritySession
     */
    public void createdSecuritySession() {
        this.securitySessionId = IdUtil.getUUIDv7();
        this.createTime = DateUtil.format(LocalDateTime.now());
        // 创建通知
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

        // 调用删除
        if (repository.removeSecuritySessionByLoginId(this.loginId)) {
            // 销毁通知
            SecurityEventPublishManager.doDestroySecuritySession(this.securitySessionId);
        }
    }

    // ----------------------------token 操作-------------------------------

    /**
     * 更新 token 信息
     *
     * @param tokenInfo TokenInfo
     */
    public SecuritySession updateTokenInfo(SecurityToken tokenInfo) {
        for (SecurityToken info : this.tokenInfoList) {
            if (info.getToken().equals(tokenInfo.getToken())) {
                if (StringUtil.isNotBlank(tokenInfo.getState())) {
                    info.setState(tokenInfo.getState());
                }
                if (null != tokenInfo.getDeviceType()) {
                    info.setDeviceType(tokenInfo.getDeviceType());
                }
                if (null != tokenInfo.getTimeout()) {
                    info.setTimeout(tokenInfo.getTimeout());
                }
                if (null != tokenInfo.getActivityTimeout()) {
                    info.setActivityTimeout(tokenInfo.getActivityTimeout());
                }
                info.setUpdateTime(DateUtil.format(LocalDateTime.now()));
                break;
            }
        }
        return this;
    }

    /**
     * 更新 token 状态
     *
     * @param token token
     * @param state 状态
     *
     * @return SecuritySession
     */
    public SecuritySession updateTokenInfoState(String token, String state) {
        SecurityToken tokenInfo = new SecurityToken();
        tokenInfo.setToken(token);
        tokenInfo.setState(state);
        return this.updateTokenInfo(tokenInfo);
    }

    /**
     * token 续约
     *
     * @param token token
     *
     * @return SecuritySession
     */
    public SecuritySession renewalToken(String token) {
        SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);
        if (repository.renewalTokenByTokenValue(token)) {
            Logs.debug(EmojiSymbol.SECURITY, "续约成功：{}", token);
        }
        return this;
    }

    /**
     * 新增 token 信息
     *
     * @param tokenInfo TokenInfo
     */
    public SecuritySession addTokenInfo(SecurityToken tokenInfo) {
        this.tokenInfoList.add(tokenInfo);
        return this;
    }

    /**
     * 删除 token 信息
     *
     * @param token token
     *
     * @return SecuritySession
     */
    public SecuritySession removeTokenInfo(String token) {
        this.tokenInfoList.remove(getTokenInfoByToken(token));
        return this;
    }

    /**
     * 根据 tokenValue 获取 token 信息
     *
     * @param token token
     *
     * @return {@link SecurityToken}
     */
    public SecurityToken getTokenInfoByToken(String token) {
        List<SecurityToken> tokenList = tokenInfoList.stream().filter(item -> item.getToken().equals(token)).toList();
        return tokenList.isEmpty() ? null : tokenList.getFirst();
    }

    /**
     * 设置存储信息
     *
     * @param key   key 值
     * @param value value 值
     *
     * @return SecuritySession
     */
    public SecuritySession setAttribute(String key, Object value) {
        mountData.put(key, value);
        flushSessionStorage();
        return fetchData();
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
     * 获取所有存储信息
     *
     * @return Object
     */
    public <T> T getAttributes(Class<T> clazz) {
        return JsonUtil.readObj(JsonUtil.toJson(mountData), clazz);
    }

    /**
     * 获取存储信息
     *
     * @param key    字符串 key
     * @param tClass 类型
     * @param <T>    泛型
     *
     * @return 转换后的对象
     */
    public <T> T getAttribute(String key, Class<T> tClass) {
        Object obj = getAttribute(key);
        return JsonUtil.readObj(JsonUtil.toJson(obj), tClass);
    }

    /**
     * 获取数据
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
     * 刷新 session 到存储
     */
    public void flushSessionStorage() {
        SecurityRepository repository = SpringUtil.getBean(SecurityRepository.class);
        // 验证失效的 token
        List<SecurityToken> invalidTokenInfoList = new ArrayList<>();
        // 最大超时时间 (小时)
        int maxTimeoutHour = 48;
        if (!this.getTokenInfoList().isEmpty()) {
            this.getTokenInfoList().forEach(securityToken -> {
                // 非可用状态的，且时间超过 48 小时，视为无效 token，进行清理
                if (StringUtil.isNotBlank(securityToken.getUpdateTime()) && (!SecurityConstant.TOKEN_STATE_NORMAL.equals(securityToken.getState()) && DateUtil.parse(securityToken.getUpdateTime()).plusHours(maxTimeoutHour).isBefore(LocalDateTime.now()))) {
                    invalidTokenInfoList.add(securityToken);

                }
                // 检查并更新 token 状态
                SecurityToken st = repository.getSecurityTokenByTokenValue(securityToken.getToken());
                if (Objects.nonNull(st)) {
                    if (!securityToken.getState().equals(st.getState())) {
                        // 状态同步
                        st.setState(securityToken.getState());
                        repository.saveToken(st);
                    }
                    securityToken.setActivityTime(st.getActivityTime());
                }
            });
        }
        invalidTokenInfoList.forEach(item -> this.removeTokenInfo(item.getToken()));

        boolean result = repository.saveSecuritySession(this);
        if (!result) {
            throw new ExtException(EmojiSymbol.SECURITY, "保存 session 认证数据失败");
        }
        // 移除无效的 token
        invalidTokenInfoList.forEach(item -> {
            if (repository.removeTokenByTokenValue(item.getToken())) {
                SecurityEventPublishManager.doRemove(this.getLoginId(), item.getToken(), item.getDeviceType());
            }
        });
    }
}
