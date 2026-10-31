package ext.library.apicrypto.strategy;

import ext.library.crypto.SM4Util;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;

/**
 * SM4 国密对称加密策略
 * <p>
 * 使用国密 SM4 算法进行加解密，采用 CBC 模式：每个请求生成随机 IV，
 * 与密文一起拼成 {@code iv:密文}（两者都是 Base64 URL 安全编码，不含 {@code :}）。
 *
 * @since 4.0.0
 */
public class SM4Strategy implements CryptoStrategy {

    @Override
    public String decrypt(String secretKey, String encryptedText, String salt) {
        String[] parts = encryptedText.split(":", 2);
        if (parts.length != 2) {
            throw new ToolException(EmojiSymbol.CRYPTO, "SM4 密文格式不正确，应为 iv:密文");
        }
        return SM4Util.decryptByCBC(secretKey, parts[0], parts[1]);
    }

    @Override
    public String encrypt(String secretKey, String plainText, String salt) {
        // ECB 下相同明文每次得到相同密文，可被直接对比出请求内容；CBC 需要每请求一个随机 IV
        String iv = SM4Util.generateKey(128);
        return iv + ":" + SM4Util.encryptByCBC(secretKey, iv, plainText);
    }

}
