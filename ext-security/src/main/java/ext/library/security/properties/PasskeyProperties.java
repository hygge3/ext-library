package ext.library.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 通行密钥（WebAuthn）配置
 * <p>
 * 配置前缀：{@code ext.security.passkey}。{@code rp-id} 未配置时不装配 PasskeyService。
 *
 * @since 4.1.1
 */
@ConfigurationProperties(PasskeyProperties.PREFIX)
public class PasskeyProperties {

    public static final String PREFIX = "ext.security.passkey";

    /**
     * 依赖方 ID，通常是站点域名（如 example.com）
     */
    private String rpId;

    /**
     * 依赖方展示名称
     */
    private String rpName = "ext";

    /**
     * 允许的来源（scheme://host[:port]），留空时按 https://{rpId} 生成
     */
    private Set<String> origins = new LinkedHashSet<>();

    /**
     * 挑战有效期
     */
    private Duration challengeTimeout = Duration.ofMinutes(5);

    /**
     * 是否强制用户验证（生物识别 / PIN）
     */
    private boolean userVerificationRequired = false;

    public String getRpId() {
        return rpId;
    }

    public void setRpId(String rpId) {
        this.rpId = rpId;
    }

    public String getRpName() {
        return rpName;
    }

    public void setRpName(String rpName) {
        this.rpName = rpName;
    }

    public Set<String> getOrigins() {
        return origins;
    }

    public void setOrigins(Set<String> origins) {
        this.origins = origins;
    }

    public Duration getChallengeTimeout() {
        return challengeTimeout;
    }

    public void setChallengeTimeout(Duration challengeTimeout) {
        this.challengeTimeout = challengeTimeout;
    }

    public boolean isUserVerificationRequired() {
        return userVerificationRequired;
    }

    public void setUserVerificationRequired(boolean userVerificationRequired) {
        this.userVerificationRequired = userVerificationRequired;
    }

}
