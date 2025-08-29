package ext.library.encrypt.strategy;

import ext.library.crypto.RSAUtil;

import jakarta.annotation.Nonnull;

/**
 * RSA 策略
 *
 * @since 2025.08.29
 */
public class RSAStrategy implements CryptoStrategy {

    @Override
    public String decrypt(@Nonnull String secretKey, @Nonnull String encryptedText, String salt) {
        return RSAUtil.decrypt(secretKey, encryptedText);

    }

    @Override
    public String encrypt(@Nonnull String secretKey, @Nonnull String plainText, String salt) {
        return RSAUtil.encrypt(secretKey, plainText);
    }
}