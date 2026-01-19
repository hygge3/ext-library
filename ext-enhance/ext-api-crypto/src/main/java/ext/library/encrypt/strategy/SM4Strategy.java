package ext.library.encrypt.strategy;

import ext.library.crypto.SM4Util;

public class SM4Strategy implements CryptoStrategy {
    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return SM4Util.decryptByECB(secretKey, encryptedText);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return SM4Util.encryptByECB(secretKey, plainText);
    }
}
