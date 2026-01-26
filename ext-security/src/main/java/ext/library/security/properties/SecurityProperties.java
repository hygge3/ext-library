package ext.library.security.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

/**
 * 安全模块配置属性
 * <p>
 * 配置前缀：{@code ext.security}
 *
 * @since 4.0.0
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
     * 授权有效期，默认 30 天
     * <p>
     * 支持 Duration 格式：30d, 720h, 2592000s 等
     */
    private Duration timeout = Duration.ofDays(30);

    /**
     * 最低活跃频率，如果 token 超过此时间没有访问系统就会被冻结，默认 1 小时
     * <p>
     * 支持 Duration 格式：1h, 60m, 3600s 等
     */
    private Duration activityTimeout = Duration.ofHours(1);

    /**
     * 是否自动续约，默认为 true
     * <p>
     * 设置为 true 时会在调用 checkToken 完成时自动调用续约方法
     */
    private Boolean autoRenewal = true;

    /**
     * 自动续约间隔时长，默认 3 分钟
     * <p>
     * 支持 Duration 格式：3m, 180s 等
     */
    private Duration autoRenewalInterval = Duration.ofMinutes(3);

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
     * cookie 配置
     */
    private CookieProperties cookieConfig = new CookieProperties();

    public String getSecurityName() {
        return securityName;
    }

    public void setSecurityName(String securityName) {
        this.securityName = securityName;
    }

    public Duration getTimeout() {
        return timeout;
    }

    /**
     * 获取超时时间（秒）
     *
     * @return 超时秒数
     */
    public long getTimeoutSeconds() {
        return timeout.toSeconds();
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public Duration getActivityTimeout() {
        return activityTimeout;
    }

    /**
     * 获取活跃超时时间（秒）
     *
     * @return 活跃超时秒数
     */
    public long getActivityTimeoutSeconds() {
        return activityTimeout.toSeconds();
    }

    public void setActivityTimeout(Duration activityTimeout) {
        this.activityTimeout = activityTimeout;
    }

    public Boolean getAutoRenewal() {
        return autoRenewal;
    }

    public void setAutoRenewal(Boolean autoRenewal) {
        this.autoRenewal = autoRenewal;
    }

    public Duration getAutoRenewalInterval() {
        return autoRenewalInterval;
    }

    /**
     * 获取自动续约间隔时间（秒）
     *
     * @return 自动续约间隔秒数
     */
    public long getAutoRenewalIntervalSeconds() {
        return autoRenewalInterval.toSeconds();
    }

    public void setAutoRenewalInterval(Duration autoRenewalInterval) {
        this.autoRenewalInterval = autoRenewalInterval;
    }

    public Boolean getIsConcurrentLogin() {
        return isConcurrentLogin;
    }

    public void setIsConcurrentLogin(Boolean isConcurrentLogin) {
        this.isConcurrentLogin = isConcurrentLogin;
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

        public Boolean getHttpOnly() {
            return httpOnly;
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
    }

}
