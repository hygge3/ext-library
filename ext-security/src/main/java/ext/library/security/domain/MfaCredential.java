package ext.library.security.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 两步验证（TOTP）凭证
 *
 * @since 4.1.1
 */
public class MfaCredential implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录 ID
     */
    private String loginId;

    /**
     * Base32 TOTP secret
     */
    private String secret;

    /**
     * 是否已完成绑定（beginSetup 之后为 false，校验一次验证码后为 true）
     */
    private boolean enabled;

    /**
     * 恢复码的 SHA-256 摘要，明文只在生成时返回一次
     */
    private List<String> recoveryCodes = new ArrayList<>();

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public MfaCredential() {
    }

    /**
     * 深拷贝
     * <p>
     * 缓存后端返回的是缓存里的同一个引用，调用方就地改写会污染缓存对象。
     *
     * @return 副本
     */
    public MfaCredential copy() {
        MfaCredential copy = new MfaCredential();
        copy.loginId = loginId;
        copy.secret = secret;
        copy.enabled = enabled;
        copy.recoveryCodes = new ArrayList<>(recoveryCodes);
        copy.createTime = createTime;
        return copy;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(List<String> recoveryCodes) {
        this.recoveryCodes = new ArrayList<>(recoveryCodes);
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

}
