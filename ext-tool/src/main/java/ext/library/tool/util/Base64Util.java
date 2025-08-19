package ext.library.tool.util;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * base64 编解码工具
 *
 * @since 2025.08.19
 */
@UtilityClass
public class Base64Util {

    // region UrlEncode withoutPadding

    /**
     * URL 安全的 Base64 编码（无填充）
     * 1、示例：SGVsbG8rV29ybGQvMTIzPw==
     * 2、场景：JWT、URL 参数、文件名
     */
    public static String encodeUrlSafeToStr(String data) {
        return new String(encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 编码（无填充）
     */
    public static String encodeUrlSafeToStr(byte[] data) {
        return new String(encodeUrlSafe(data), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 编码（无填充）
     */
    public static byte[] encodeUrlSafe(String data) {
        return encodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL 安全的 Base64 编码（无填充）
     */
    public static byte[] encodeUrlSafe(byte[] data) {
        return Base64.getUrlEncoder().encode(data);
    }

    // endregion
    // region UrlDecode

    /**
     * URL 安全的 Base64 解码
     */
    public static String decodeUrlSafeToStr(String data) {
        return new String(decodeUrlSafe(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 解码
     */
    public static String decodeUrlSafeToStr(byte[] data) {
        return new String(decodeUrlSafe(data), StandardCharsets.UTF_8);
    }

    /**
     * URL 安全的 Base64 解码
     */
    public static byte[] decodeUrlSafe(String data) {
        return decodeUrlSafe(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * URL 安全的 Base64 解码
     */
    public static byte[] decodeUrlSafe(byte[] data) {
        return Base64.getUrlDecoder().decode(data);
    }

    // endregion
    // region Standard  Encode

    /**
     * 标准 Base64 编码
     * 1、示例：SGVsbG8rV29ybGQvMTIzPw
     * 2、场景：普通字符串、二进制数据、邮件
     */
    public static String encodeToStr(String data) {
        return new String(encode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 编码
     */
    public static String encodeToStr(byte[] data) {
        return new String(encode(data), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 编码
     */
    public static byte[] encode(String data) {
        return encode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准 Base64 编码
     */
    public static byte[] encode(byte[] data) {
        return Base64.getEncoder().encode(data);
    }

    // endregion
    // region Standard Decode

    /**
     * 标准 Base64 解码
     */
    public static String decodeToStr(String data) {
        return new String(decode(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 解码
     */
    public static String decodeToStr(byte[] data) {
        return new String(decode(data), StandardCharsets.UTF_8);
    }

    /**
     * 标准 Base64 解码
     */
    public static byte[] decode(String data) {
        return decode(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准 Base64 解码
     */
    public static byte[] decode(byte[] data) {
        return Base64.getDecoder().decode(data);
    }

    // endregion

}