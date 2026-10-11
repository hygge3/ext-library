package ext.library.apicrypto.strategy;

import ext.library.crypto.RSAUtil;

/**
 * RSA 非对称加密策略
 * <p>
 * 使用 RSA 算法进行加解密，加密使用公钥，解密使用私钥。
 *
 * @since 4.0.0
 */
public class RSAStrategy implements CryptoStrategy {

    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return RSAUtil.decrypt(secretKey, encryptedText);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return RSAUtil.encrypt(secretKey, plainText);
    }
}
