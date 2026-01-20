package ext.library.apicrypto.strategy;

import ext.library.crypto.SM4Util;

/**
 * SM4 国密对称加密策略
 * <p>
 * 使用国密 SM4 算法进行加解密，采用 ECB 模式。
 *
 * @since 4.0.0
 */
public class SM4Strategy implements CryptoStrategy {

    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        return SM4Util.decryptByECB(secretKey, encryptedText);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        return SM4Util.encryptByECB(secretKey, plainText);
    }
}
