package ext.library.tool.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Base64Util 工具类测试")
class Base64UtilTest {
    final String str = "单元测试";
    final String base64UrlStr = "5Y2V5YWD5rWL6K-V";
    final String base64Str = "5Y2V5YWD5rWL6K+V";

    @Test
    @DisplayName("测试 URL 安全的 Base64 编码")
    void encodeUrlSafe() {
        assertEquals(base64UrlStr, Base64Util.encodeUrlSafeToStr(str));
        assertEquals(base64UrlStr, Base64Util.encodeUrlSafeToStr(str.getBytes()));
        assertArrayEquals(base64UrlStr.getBytes(), Base64Util.encodeUrlSafe(str));
        assertArrayEquals(base64UrlStr.getBytes(), Base64Util.encodeUrlSafe(str.getBytes()));
    }

    @Test
    @DisplayName("测试 URL 安全的 Base64 解码")
    void decodeUrlSafe() {
        assertEquals(str, Base64Util.decodeUrlSafeToStr(base64UrlStr));
        assertEquals(str, Base64Util.decodeUrlSafeToStr(base64UrlStr.getBytes()));
        assertArrayEquals(str.getBytes(), Base64Util.decodeUrlSafe(base64UrlStr));
        assertArrayEquals(str.getBytes(), Base64Util.decodeUrlSafe(base64UrlStr.getBytes()));

    }

    @Test
    @DisplayName("测试 Base64 编码")
    void encode() {
        assertEquals(base64Str, Base64Util.encodeToStr(str));
        assertEquals(base64Str, Base64Util.encodeToStr(str.getBytes()));
        assertArrayEquals(base64Str.getBytes(), Base64Util.encode(str));
        assertArrayEquals(base64Str.getBytes(), Base64Util.encode(str.getBytes()));
    }

    @Test
    @DisplayName("测试 Base64 解码")
    void decode() {
        assertEquals(str, Base64Util.decodeToStr(base64Str));
        assertEquals(str, Base64Util.decodeToStr(base64Str.getBytes()));
        assertArrayEquals(str.getBytes(), Base64Util.decode(base64Str));
        assertArrayEquals(str.getBytes(), Base64Util.decode(base64Str.getBytes()));
    }
}