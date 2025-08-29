package ext.library.encrypt.strategy;

import ext.library.crypto.DESUtil;

import jakarta.annotation.Nonnull;

public class DESStrategy implements CryptoStrategy {
    @Override
    public String decrypt(@Nonnull String secretKey, @Nonnull String encryptedText, String salt) {
        return DESUtil.decrypt(secretKey, encryptedText);
    }

    @Override
    public String encrypt(String secretKey, @Nonnull String plainText, String salt) {
        return DESUtil.encrypt(secretKey, plainText);
    }
}