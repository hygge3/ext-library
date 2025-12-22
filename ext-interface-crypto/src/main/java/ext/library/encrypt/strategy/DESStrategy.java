package ext.library.encrypt.strategy;

import ext.library.crypto.DESUtil;

public class DESStrategy implements CryptoStrategy {
    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return DESUtil.decrypt(secretKey, encryptedText);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return DESUtil.encrypt(secretKey, plainText);
    }
}
