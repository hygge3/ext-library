package ext.library.encrypt.strategy;

import ext.library.crypto.SM2Util;

import jakarta.annotation.Nonnull;

public class SM2Strategy implements CryptoStrategy {
    @Override
    public String decrypt(@Nonnull String secretKey, @Nonnull String encryptedText, String salt) {
        return SM2Util.decrypt(secretKey, encryptedText);
    }

    @Override
    public String encrypt(@Nonnull String secretKey, @Nonnull String plainText, String salt) {
        return SM2Util.encrypt(secretKey, plainText);
    }
}