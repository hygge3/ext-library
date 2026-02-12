package ext.library.tool.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编解码工具类
 * <p>
 * 提供标准 Base64 和 URL 安全 Base64 的编解码功能。
 * <ul>
 *   <li>标准编码：使用 {@code +} 和 {@code /}，有填充（{@code =}）</li>
 *   <li>URL 安全编码：使用 {@code -} 和 {@code _}，无填充</li>
 * </ul>
 *
 * @since 2025.01.01
 */
public final class Base64Util {

    private Base64Util() {
    }

    // region URL 安全编码（无填充）

    /**
     * URL 安全的 Base64 编码（无填充）
     * <p>
     * 示例：{@code SGVsbG8rV29ybGQvMTIzPw}（注意末尾无 {@code =}）
     * <p>
     * 适用场景：JWT、URL 参数、文件名
     *
     * @param data 原始字符串
     *
     * @return 编码后的字符串
     */
    public static String encodeUrlSafeToStr(String data) {
        return new String(encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 编码（无填充）
     *
     * @param data 原始字节数组
     *
     * @return 编码后的字符串
     */
    public static String encodeUrlSafeToStr(byte[] data) {
        return new String(encodeUrlSafe(data), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 编码（无填充）
     *
     * @param data 原始字符串
     *
     * @return 编码后的字节数组
     */
    public static byte[] encodeUrlSafe(String data) {
        return encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL 安全的 Base64 编码（无填充）
     *
     * @param data 原始字节数组
     *
     * @return 编码后的字节数组
     */
    public static byte[] encodeUrlSafe(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encode(data);
    }

    // endregion

    // region URL 安全解码

    /**
     * URL 安全的 Base64 解码
     *
     * @param data 编码后的字符串
     *
     * @return 解码后的原始字符串
     */
    public static String decodeUrlSafeToStr(String data) {
        return new String(decodeUrlSafe(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 解码
     *
     * @param data 编码后的字节数组
     *
     * @return 解码后的原始字符串
     */
    public static String decodeUrlSafeToStr(byte[] data) {
        return new String(decodeUrlSafe(data), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 解码
     *
     * @param data 编码后的字符串
     *
     * @return 解码后的字节数组
     */
    public static byte[] decodeUrlSafe(String data) {
        return decodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL 安全的 Base64 解码
     *
     * @param data 编码后的字节数组
     *
     * @return 解码后的字节数组
     */
    public static byte[] decodeUrlSafe(byte[] data) {
        return Base64.getUrlDecoder().decode(data);
    }

    // endregion

    // region 标准编码（有填充）

    /**
     * 标准 Base64 编码
     * <p>
     * 示例：{@code SGVsbG8rV29ybGQvMTIzPw==}（注意末尾有 {@code =} 填充）
     * <p>
     * 适用场景：通用数据传输、邮件附件
     *
     * @param data 原始字符串
     *
     * @return 编码后的字符串
     */
    public static String encodeToStr(String data) {
        return new String(encode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 编码
     *
     * @param data 原始字节数组
     *
     * @return 编码后的字符串
     */
    public static String encodeToStr(byte[] data) {
        return new String(encode(data), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 编码
     *
     * @param data 原始字符串
     *
     * @return 编码后的字节数组
     */
    public static byte[] encode(String data) {
        return encode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准 Base64 编码
     *
     * @param data 原始字节数组
     *
     * @return 编码后的字节数组
     */
    public static byte[] encode(byte[] data) {
        return Base64.getEncoder().encode(data);
    }

    // endregion

    // region 标准解码

    /**
     * 标准 Base64 解码
     *
     * @param data 编码后的字符串
     *
     * @return 解码后的原始字符串
     */
    public static String decodeToStr(String data) {
        return new String(decode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 解码
     *
     * @param data 编码后的字节数组
     *
     * @return 解码后的原始字符串
     */
    public static String decodeToStr(byte[] data) {
        return new String(decode(data), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 解码
     *
     * @param data 编码后的字符串
     *
     * @return 解码后的字节数组
     */
    public static byte[] decode(String data) {
        return decode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准 Base64 解码
     *
     * @param data 编码后的字节数组
     *
     * @return 解码后的字节数组
     */
    public static byte[] decode(byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    // endregion
}
