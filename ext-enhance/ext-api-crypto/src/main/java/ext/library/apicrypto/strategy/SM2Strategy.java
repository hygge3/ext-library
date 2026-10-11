package ext.library.apicrypto.strategy;

import ext.library.crypto.SM2Util;

/**
 * SM2 国密非对称加密策略
 * <p>
 * 使用国密 SM2 算法进行加解密，加密使用公钥，解密使用私钥。
 *
 * @since 4.0.0
 */
public class SM2Strategy implements CryptoStrategy {

    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return SM2Util.decrypt(secretKey, encryptedText);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return SM2Util.encrypt(secretKey, plainText);
    }
}
