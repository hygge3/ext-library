package ext.library.apicrypto.strategy;

import ext.library.tool.util.Base64Util;

/**
 * Base64 编码策略
 * <p>
 * 使用 URL 安全的 Base64 进行编解码。注意：Base64 仅是编码而非加密，不提供安全性保护。
 *
 * @since 4.0.0
 */
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
