package ext.library.crypto;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.HexFormat;

/**
 * SM3 国密摘要算法工具类，提供 SM3 哈希计算功能
 *
 * <p>设计目的：封装国密 SM3 摘要算法，提供简单易用的 API</p>
 *
 * <p>算法特性：
 * <ul>
 *   <li>输出长度：256 位（32 字节）</li>
 *   <li>分组长度：512 位</li>
 *   <li>安全性：与 SHA-256 相当</li>
 *   <li>适用于数字签名、消息认证码、随机数生成等</li>
 * </ul>
 * </p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>计算字符串的 SM3 哈希值</li>
 *   <li>计算字节数组的 SM3 哈希值</li>
 *   <li>计算 HMAC-SM3 消息认证码</li>
 * </ul>
 * </p>
 *
 * <p>使用示例：
 * <pre>
 * // 计算字符串哈希
 * String hash = SM3Util.hash("hello world");
 *
 * // 计算字节数组哈希
 * byte[] hashBytes = SM3Util.hashToBytes("hello world".getBytes());
 *
 * // 计算 HMAC-SM3
 * String hmac = SM3Util.hmac("secret-key", "message");
 * </pre>
 * </p>
 *
 * @since 4.0.0
 */
public final class SM3Util {

    /**
     * SM3 摘要长度（字节）
     */
    private static final int digestLength = 32;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private SM3Util() {
        // 私有构造函数，防止实例化
    }

    /**
     * 计算字符串的 SM3 哈希值
     *
     * @param input 输入字符串
     *
     * @return Hex 编码的哈希值（64 个十六进制字符）
     */
    public static String hash(String input) {
        byte[] hashBytes = hashToBytes(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hashBytes);
    }

    /**
     * 计算字节数组的 SM3 哈希值
     *
     * @param input 输入字节数组
     *
     * @return 哈希值字节数组（32 字节）
     */
    public static byte[] hashToBytes(byte[] input) {
        SM3Digest digest = new SM3Digest();
        digest.update(input, 0, input.length);
        byte[] result = new byte[digestLength];
        digest.doFinal(result, 0);
        return result;
    }

    /**
     * 计算 HMAC-SM3 消息认证码
     *
     * @param key     密钥
     * @param message 消息
     *
     * @return Hex 编码的 HMAC 值
     */
    public static String hmac(String key, String message) {
        byte[] hmacBytes = hmacToBytes(
                key.getBytes(StandardCharsets.UTF_8),
                message.getBytes(StandardCharsets.UTF_8)
        );
        return HexFormat.of().formatHex(hmacBytes);
    }

    /**
     * 计算 HMAC-SM3 消息认证码
     *
     * @param key     密钥字节数组
     * @param message 消息字节数组
     *
     * @return HMAC 值字节数组（32 字节）
     */
    public static byte[] hmacToBytes(byte[] key, byte[] message) {
        HMac hmac = new HMac(new SM3Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(message, 0, message.length);
        byte[] result = new byte[hmac.getMacSize()];
        hmac.doFinal(result, 0);
        return result;
    }

    /**
     * 验证哈希值是否匹配
     *
     * @param input        原始输入
     * @param expectedHash 期望的哈希值（Hex 编码）
     *
     * @return 如果哈希值匹配返回 true
     */
    public static boolean verify(String input, String expectedHash) {
        String actualHash = hash(input);
        return actualHash.equalsIgnoreCase(expectedHash);
    }

    /**
     * 验证 HMAC 值是否匹配
     *
     * @param key          密钥
     * @param message      消息
     * @param expectedHmac 期望的 HMAC 值（Hex 编码）
     *
     * @return 如果 HMAC 值匹配返回 true
     */
    public static boolean verifyHmac(String key, String message, String expectedHmac) {
        String actualHmac = hmac(key, message);
        return actualHmac.equalsIgnoreCase(expectedHmac);
    }
}
