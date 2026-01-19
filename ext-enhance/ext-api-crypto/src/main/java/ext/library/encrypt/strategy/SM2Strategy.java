package ext.library.encrypt.strategy;

import ext.library.crypto.SM2Util;

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
