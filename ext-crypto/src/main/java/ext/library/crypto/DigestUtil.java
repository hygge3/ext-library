package ext.library.crypto;


import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 摘要算法工具类，提供常用的哈希摘要计算功能
 *
 * <p>设计目的：封装 Java 标准库的 MessageDigest，提供简单易用的哈希计算 API</p>
 *
 * <p>支持的算法（取决于 JVM 实现）：
 * <ul>
 *   <li>MD5 - 128 位摘要（不推荐用于安全场景）</li>
 *   <li>SHA-1 - 160 位摘要（不推荐用于安全场景）</li>
 *   <li>SHA-256 - 256 位摘要（推荐）</li>
 *   <li>SHA-384 - 384 位摘要</li>
 *   <li>SHA-512 - 512 位摘要</li>
 *   <li>SHA3-256/384/512 - SHA-3 系列（推荐）</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * String sha256Hash = DigestUtil.hash("SHA-256", "data");
 * boolean available = DigestUtil.isAlgorithmAvailable("SHA3-256");
 * </pre>
 * </p>
 *
 * @since 2025.08.19
 */
public final class DigestUtil {

    private DigestUtil() {
        // 私有构造函数，防止实例化
    }

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
