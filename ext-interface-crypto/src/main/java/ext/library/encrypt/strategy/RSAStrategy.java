package ext.library.encrypt.strategy;

import ext.library.crypto.RSAUtil;

/**
 * RSA 策略
 *
 * @since 2025.08.29
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
