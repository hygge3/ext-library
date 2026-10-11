package ext.library.apicrypto.strategy;

/**
 * 加密策略接口
 * <p>
 * 定义加密和解密操作的抽象接口，所有具体加密算法策略需实现此接口。
 *
 * @since 4.0.0
 */
public interface CryptoStrategy {

    /**
     * 解密
     *
     * @param secretKey     密钥（对称加密）或私钥（非对称加密）
     * @param encryptedText 加密后的文本（Base64 编码）
     * @param salt          盐值，仅部分算法支持（如 AES），其他算法可忽略
     * @return 解密后的明文
     */
    String decrypt(String secretKey, String encryptedText, String salt);

    /**
     * 加密
     *
     * @param secretKey 密钥（对称加密）或公钥（非对称加密）
     * @param plainText 待加密的明文
     * @param salt      盐值，仅部分算法支持（如 AES），其他算法可忽略
     * @return 加密后的文本（Base64 编码）
     */
    String encrypt(String secretKey, String plainText, String salt);
}
