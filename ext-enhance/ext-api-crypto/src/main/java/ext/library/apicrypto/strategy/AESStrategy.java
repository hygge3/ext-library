package ext.library.apicrypto.strategy;

import ext.library.crypto.AESUtil;

/**
 * AES 对称加密策略
 * <p>
 * 使用 AES 算法进行加解密，支持盐值增强安全性。
 *
 * @since 4.0.0
 */
public class AESStrategy implements CryptoStrategy {

    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return AESUtil.decrypt(secretKey, encryptedText, salt);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return AESUtil.encrypt(secretKey, plainText, salt);
    }
}
