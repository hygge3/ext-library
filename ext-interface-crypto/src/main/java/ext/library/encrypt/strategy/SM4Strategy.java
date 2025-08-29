package ext.library.encrypt.strategy;

import ext.library.crypto.SM4Util;

import jakarta.annotation.Nonnull;

public class SM4Strategy implements CryptoStrategy {
    @Override
    public String decrypt(@Nonnull String secretKey, @Nonnull String encryptedText, String salt) {
        return SM4Util.decryptByECB(secretKey, encryptedText);
    }

    @Override
    public String encrypt(@Nonnull String secretKey, @Nonnull String plainText, String salt) {
        return SM4Util.encryptByECB(secretKey, plainText);
    }
}