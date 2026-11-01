package ext.library.security.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 通行密钥（WebAuthn）凭证
 * <p>
 * 公钥不单独存字段，直接存 webauthn4j 的 AttestedCredentialData（CBOR 的 Base64URL 编码），
 * 校验断言时原样还原成对象，避免有损的字段拆分。
 *
 * @since 4.1.1
 */
public class PasskeyCredential implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 登录 ID
     */
    private String loginId;

    /**
     * 凭证 ID（Base64URL）
     */
    private String credentialId;

    /**
     * AttestedCredentialData 的 CBOR 字节（Base64URL）
     */
    private String attestedCredentialData;

    /**
     * 签名计数器
     */
    private long counter;

    /**
     * 传输方式（usb / nfc / ble / internal / hybrid）
     */
    private List<String> transports = new ArrayList<>();

    /**
     * 用户验证是否已初始化
     */
    private Boolean uvInitialized;

    /**
     * 是否可备份（可同步到云端）
     */
    private Boolean backupEligible;

    /**
     * 是否已备份
     */
    private Boolean backedUp;

    /**
     * 注册时间
     */
    private LocalDateTime createTime;

    /**
     * 最近一次使用时间
     */
    private LocalDateTime lastUsedTime;

    public PasskeyCredential() {
    }

    /**
     * 深拷贝
     * <p>
     * 缓存后端返回的是缓存里的同一个引用，调用方就地改写会污染缓存对象。
     *
     * @return 副本
     */
    public PasskeyCredential copy() {
        PasskeyCredential copy = new PasskeyCredential();
        copy.loginId = loginId;
        copy.credentialId = credentialId;
        copy.attestedCredentialData = attestedCredentialData;
        copy.counter = counter;
        copy.transports = new ArrayList<>(transports);
        copy.uvInitialized = uvInitialized;
        copy.backupEligible = backupEligible;
        copy.backedUp = backedUp;
        copy.createTime = createTime;
        copy.lastUsedTime = lastUsedTime;
        return copy;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getAttestedCredentialData() {
        return attestedCredentialData;
    }

    public void setAttestedCredentialData(String attestedCredentialData) {
        this.attestedCredentialData = attestedCredentialData;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public List<String> getTransports() {
        return transports;
    }

    public void setTransports(List<String> transports) {
        this.transports = new ArrayList<>(transports);
    }

    public Boolean getUvInitialized() {
        return uvInitialized;
    }

    public void setUvInitialized(Boolean uvInitialized) {
        this.uvInitialized = uvInitialized;
    }

    public Boolean getBackupEligible() {
        return backupEligible;
    }

    public void setBackupEligible(Boolean backupEligible) {
        this.backupEligible = backupEligible;
    }

    public Boolean getBackedUp() {
        return backedUp;
    }

    public void setBackedUp(Boolean backedUp) {
        this.backedUp = backedUp;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(LocalDateTime lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

}
