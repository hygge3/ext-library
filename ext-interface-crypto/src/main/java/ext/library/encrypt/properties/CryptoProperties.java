package ext.library.encrypt.properties;

import ext.library.encrypt.enums.Algorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 加解密自动配置属性
 */
@Getter
@Setter
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
}