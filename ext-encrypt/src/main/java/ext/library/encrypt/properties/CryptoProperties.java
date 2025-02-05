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
	private Algorithm algo;

	/**
	 * RSA 公钥
	 */
	private String publicKey;

	/**
	 * RSA 私钥
	 */
	private String secretKey;

}
