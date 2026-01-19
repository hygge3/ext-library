package ext.library.encrypt.strategy;

/**
 * 加密策略
 *
 * @since 2025.08.29
 */
public interface CryptoStrategy {
    /**
     * 解密
     *
     * @param secretKey     密钥/公钥
     * @param encryptedText 加密文本
     * @param salt          盐
     *
     * @return {@link String }
     */
    String decrypt(String secretKey, String encryptedText, String salt);

    /**
     * 加密
     *
     * @param secretKey 密钥/公钥
     * @param plainText 纯文本
     * @param salt      盐
     *
     * @return {@link String }
     */
    String encrypt(String secretKey, String plainText, String salt);
}
