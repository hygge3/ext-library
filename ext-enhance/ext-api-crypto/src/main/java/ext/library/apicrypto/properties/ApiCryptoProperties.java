package ext.library.apicrypto.properties;

import ext.library.apicrypto.enums.Algorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * API 加解密配置属性
 * <p>
 * 配置示例：
 * <pre>{@code
 * ext:
 *   api-crypto:
 *     algorithm: RSA
 *     public-key: "MIGfMA0GCSqGSIb3DQEBAQUAA4..."
 *     private-key: "MIICdQIBADANBgkqhkiG9w0BAQ..."
 * }</pre>
 *
 * @since 4.0.0
 */
@ConfigurationProperties(ApiCryptoProperties.PREFIX)
public class ApiCryptoProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "ext.api-crypto";

    /**
     * 加密算法，默认为 RSA
     */
    private Algorithm algorithm = Algorithm.RSA;

    /**
     * 公钥（用于 RSA、SM2 非对称加密的加密操作）
     */
    private String publicKey;

    /**
     * 私钥（用于 RSA、SM2 非对称加密的解密操作）
     */
    private String privateKey;

    /**
     * 密钥（用于 AES、SM4 对称加密）
     */
    private String secretKey;

    /**
     * 盐值（仅 AES 算法使用）
     */
    private String salt;

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * 获取用于加密的密钥
     * <p>
     * 非对称加密使用公钥，对称加密使用密钥。
     *
     * @return 加密密钥
     */
    public String getEncryptKey() {
        return publicKey != null && !publicKey.isBlank() ? publicKey : secretKey;
    }

    /**
     * 获取用于解密的密钥
     * <p>
     * 非对称加密使用私钥，对称加密使用密钥。
     *
     * @return 解密密钥
     */
    public String getDecryptKey() {
        return privateKey != null && !privateKey.isBlank() ? privateKey : secretKey;
    }
}
