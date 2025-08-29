package ext.library.encrypt.strategy;

import ext.library.tool.util.Base64Util;

import jakarta.annotation.Nonnull;

public class Base64Strategy implements CryptoStrategy {
    @Override
    public String decrypt(String secretKey, @Nonnull String encryptedText, String salt) {
        return Base64Util.decodeToStr(encryptedText);
    }

    @Override
    public String encrypt(String secretKey, @Nonnull String plainText, String salt) {
        return Base64Util.encodeToStr(plainText);
    }
}