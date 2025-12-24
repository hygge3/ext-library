package ext.library.crypto;


import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 摘要算法工具
 *
 * @since 2025.08.19
 */
public class DigestUtil {

    /**
     * 通用哈希方法
     *
     * @param algorithm 算法名称（MD2,MD5,SHA-1,SHA-224,SHA-256,SHA-384,SHA-512,SHA-512/224,SHA-512/256,SHA3-224,SHA3-256,SHA3-384,SHA3-512 等）
     * @param input     输入字符串
     *
     * @return 哈希值（十六进制）
     */
    public static String hash(String algorithm, String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new ToolException(EmojiSymbol.CRYPTO, "不支持的算法：{}" + algorithm);
        }
    }

    /**
     * 检查算法是否可用
     */
    public static boolean isAlgorithmAvailable(String algorithm) {
        try {
            MessageDigest.getInstance(algorithm);
            return true;
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }
}
