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
    public static final String PREFIX = "ext.crypto";

    /** 算法 */
    Algorithm algo;

    /**
     * 公钥
     */
    String publicKey;

    /**
     * 私钥
     */
    String privateKey;

    /**
     * 密钥
     */
    String secretKey;

    /** 盐 */
    String salt;
}