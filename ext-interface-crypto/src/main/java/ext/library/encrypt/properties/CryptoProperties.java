package ext.library.encrypt.properties;

import ext.library.encrypt.enums.Algorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密自动配置属性
 */
@ConfigurationProperties(CryptoProperties.PREFIX)
public class CryptoProperties {

    /**
     * Prefix of {@link CryptoProperties}.
     */
    public static final String PREFIX = "ext.interface-crypto";

    /** 算法 */
    private Algorithm algo;

    /**
     * 公钥，RSA、SM2
     */
    private String publicKey;

    /**
     * 私钥，RSA、SM2
     */
    private String privateKey;

    /**
     * 密钥，AES、SM4、DES
     */
    private String secretKey;

    /** 盐，AES */
    private String salt;

    public Algorithm getAlgo() {
        return algo;
    }

    public void setAlgo(Algorithm algo) {
        this.algo = algo;
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
}