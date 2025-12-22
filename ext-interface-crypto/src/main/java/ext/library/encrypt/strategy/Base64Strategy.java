package ext.library.encrypt.strategy;

import ext.library.tool.util.Base64Util;

public class Base64Strategy implements CryptoStrategy {
    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return Base64Util.decodeUrlSafeToStr(encryptedText);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return Base64Util.encodeUrlSafeToStr(plainText);
    }
}
