package ext.library.encrypt.strategy;

import ext.library.crypto.AESUtil;

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
