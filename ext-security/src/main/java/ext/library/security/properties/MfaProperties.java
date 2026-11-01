package ext.library.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 两步验证（TOTP）配置
 * <p>
 * 配置前缀：{@code ext.security.mfa}
 *
 * @since 4.1.1
 */
@ConfigurationProperties(MfaProperties.PREFIX)
public class MfaProperties {

    public static final String PREFIX = "ext.security.mfa";

    /**
     * 发行方名称，显示在验证器 App 里
     */
    private String issuer = "ext";

    /**
     * 时间片容差，1 表示接受前后各一个时间片（±30 秒）
     */
    private Integer window = 1;

    /**
     * 恢复码数量
     */
    private Integer recoveryCodeCount = 10;

    /**
     * 未确认绑定的 secret 保留时长
     */
    private Duration setupTimeout = Duration.ofMinutes(10);

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public Integer getWindow() {
        return window;
    }

    public void setWindow(Integer window) {
        this.window = window;
    }

    public Integer getRecoveryCodeCount() {
        return recoveryCodeCount;
    }

    public void setRecoveryCodeCount(Integer recoveryCodeCount) {
        this.recoveryCodeCount = recoveryCodeCount;
    }

    public Duration getSetupTimeout() {
        return setupTimeout;
    }

    public void setSetupTimeout(Duration setupTimeout) {
        this.setupTimeout = setupTimeout;
    }

}
