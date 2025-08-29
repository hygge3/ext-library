package ext.library.encrypt.strategy;

import ext.library.crypto.AESUtil;

import jakarta.annotation.Nonnull;

public class AESStrategy implements CryptoStrategy {
    @Override
    public String decrypt(@Nonnull String secretKey, @Nonnull String encryptedText, @Nonnull String salt) {
        return AESUtil.decrypt(secretKey, encryptedText, salt);
    }

    @Override
    public String encrypt(String secretKey, @Nonnull String plainText, String salt) {
        return AESUtil.encrypt(secretKey, plainText, salt);
    }
}