package ext.library.security.config.properties;

import ext.library.security.enums.SecurityRepositoryEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

/**
 * <p>
 * 配置文件
 * </p>
 */
@ConfigurationProperties(SecurityProperties.PREFIX)
@Validated
public class SecurityProperties {

    static final String PREFIX = "ext.security";

    /**
     * 认证名称
     */
    private String securityName = "Authorization";

    /**
     * 授权有效期（单位：秒）默认 30 天，-1 代表永久
     */
    private Long timeout = 60 * 60 * 24 * 30L;

    /**
     * 最低活跃频率（单位：秒），如果 token 超过此时间没有访问系统就会被冻结，，默认 30 分钟，-1 代表不限制，永不冻结
     */
    private Long activityTimeout = 60 * 60L;

    /**
     * 是否自动续约 默认为 true 设置为 true 时会在调用 checkToken 完成时自动调用续约方法
     */
    private Boolean autoRenewal = true;

    /**
     * 自动续约间隔时长（单位：秒）
     */
    private Long autoRenewalIntervalTime = 180L;

    /**
     * 同一账号，多地同时登录 true 表示允许一起登录，false 表示新登录会挤掉旧登录
     */
    private Boolean isConcurrentLogin = true;

    /**
     * 同一账号，允许最大登录数量 -1 表示不限制（当 isConcurrentLogin 为 true 时此配置项才有效）
     */
    private Integer maxLoginLimit = -1;

    /**
     * 同一账号，允许同时登录的设备类型数量， -1 表示不限制
     */
    private Integer maxLoginDeviceTypeLimit = -1;

    /**
     * 颁发 token 最大限制 -1 表示不限制
     */
    private Integer issueTokenMaxLimit = -1;

    /**
     * 是否开启 cookie 默认开启
     */
    private Boolean enableCookie = true;

    /**
     * security 存储库
     */
    private SecurityRepositoryEnum repository = SecurityRepositoryEnum.RAM;

    /**
     * cookie 配置
     */
    private CookieProperties cookieConfig = new CookieProperties();

    public String getSecurityName() {
        return securityName;
    }

    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public Long getActivityTimeout() {
        return activityTimeout;
    }

    public void setActivityTimeout(Long activityTimeout) {
        this.activityTimeout = activityTimeout;
    }

    public Boolean getAutoRenewal() {
        return autoRenewal;
    }

    public void setAutoRenewal(Boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }

    public Long getAutoRenewalIntervalTime() {
        return autoRenewalIntervalTime;
    }

    public void setAutoRenewalIntervalTime(Long autoRenewalIntervalTime) {
        this.autoRenewalIntervalTime = autoRenewalIntervalTime;
    }

    public Boolean getConcurrentLogin() {
        return isConcurrentLogin;
    }

    public void setConcurrentLogin(Boolean concurrentLogin) {
        isConcurrentLogin = concurrentLogin;
    }

    public Integer getMaxLoginLimit() {
        return maxLoginLimit;
    }

    public void setMaxLoginLimit(Integer maxLoginLimit) {
        this.maxLoginLimit = maxLoginLimit;
    }

    public Integer getMaxLoginDeviceTypeLimit() {
        return maxLoginDeviceTypeLimit;
    }

    public void setMaxLoginDeviceTypeLimit(Integer maxLoginDeviceTypeLimit) {
        this.maxLoginDeviceTypeLimit = maxLoginDeviceTypeLimit;
    }

    public Integer getIssueTokenMaxLimit() {
        return issueTokenMaxLimit;
    }

    public void setIssueTokenMaxLimit(Integer issueTokenMaxLimit) {
        this.issueTokenMaxLimit = issueTokenMaxLimit;
    }

    public Boolean getEnableCookie() {
        return enableCookie;
    }

    public void setEnableCookie(Boolean enableCookie) {
        this.enableCookie = enableCookie;
    }

    public SecurityRepositoryEnum getRepository() {
        return repository;
    }

    public void setRepository(SecurityRepositoryEnum repository) {
        this.repository = repository;
    }

    public CookieProperties getCookieConfig() {
        return cookieConfig;
    }

    public void setCookieConfig(CookieProperties cookieConfig) {
        this.cookieConfig = cookieConfig;
    }

    public static class CookieProperties {

        /**
         * cookie 名称
         */
        private final String cookieName = "Token";
        /**
         * 是否禁止 js 操作 Cookie
         */
        private final Boolean httpOnly = true;
        /**
         * 域设置
         */
        private String domain;
        /**
         * 路径设置
         */
        @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
        private String path;
        /**
         * 是否应该只在加密的（即 SSL）连接上发送
         */
        private Boolean secure;

        public String getCookieName() {
            return cookieName;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Boolean getSecure() {
            return secure;
        }

        public void setSecure(Boolean secure) {
            this.secure = secure;
        }

        public Boolean getHttpOnly() {
            return httpOnly;
        }
    }

}