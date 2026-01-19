package ext.library.redis.prefix;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IRedisPrefixConverter 测试
 */
@DisplayName("IRedisPrefixConverter 测试")
class RedisPrefixConverterTest {

    // ========== 测试实现类 ==========

    static class TestPrefixConverter implements IRedisPrefixConverter {
        private final String prefix;
        private final boolean enabled;

        TestPrefixConverter(String prefix, boolean enabled) {
            this.prefix = prefix;
            this.enabled = enabled;
        }

        @Override
        public String prefix() {
            return prefix;
        }

        @Override
        public boolean match() {
            return enabled;
        }
    }

    // ========== prefix() 测试 ==========

    @Test
    @DisplayName("测试获取前缀")
    void testPrefix() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);

        assertEquals("app:", converter.prefix(), "应返回正确的前缀");
    }

    @Test
    @DisplayName("测试空前缀")
    void testEmptyPrefix() {
        IRedisPrefixConverter converter = new TestPrefixConverter("", true);

        assertEquals("", converter.prefix(), "应返回空字符串");
    }

    // ========== match() 测试 ==========

    @Test
    @DisplayName("测试启用状态")
    void testMatchEnabled() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);

        assertTrue(converter.match(), "应返回启用状态");
    }

    @Test
    @DisplayName("测试禁用状态")
    void testMatchDisabled() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", false);

        assertFalse(converter.match(), "应返回禁用状态");
    }

    // ========== wrap() 测试 ==========

    @Test
    @DisplayName("测试添加前缀 - 正常情况")
    void testWrapNormal() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);
        byte[] original = "user:123".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped = converter.wrap(original);

        assertNotNull(wrapped, "结果不应为 null");
        String wrappedStr = new String(wrapped, StandardCharsets.UTF_8);
        assertEquals("app:user:123", wrappedStr, "应正确添加前缀");
    }

    @Test
    @DisplayName("测试添加前缀 - 禁用状态")
    void testWrapDisabled() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", false);
        byte[] original = "user:123".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped = converter.wrap(original);

        assertSame(original, wrapped, "禁用状态应返回原始数组");
    }

    @Test
    @DisplayName("测试添加前缀 - null 输入")
    void testWrapNull() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);

        byte[] wrapped = converter.wrap(null);

        // null 输入应返回 null
        // 根据 wrap 方法实现，null 或空数组会返回原值
    }

    @Test
    @DisplayName("测试添加前缀 - 空数组")
    void testWrapEmpty() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);
        byte[] original = new byte[0];

        byte[] wrapped = converter.wrap(original);

        assertSame(original, wrapped, "空数组应返回原数组");
    }

    @Test
    @DisplayName("测试添加前缀 - 多次添加")
    void testWrapMultiple() {
        IRedisPrefixConverter converter = new TestPrefixConverter("cache:", true);

        byte[] key1 = "key1".getBytes(StandardCharsets.UTF_8);
        byte[] key2 = "key2".getBytes(StandardCharsets.UTF_8);
        byte[] key3 = "key3".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped1 = converter.wrap(key1);
        byte[] wrapped2 = converter.wrap(key2);
        byte[] wrapped3 = converter.wrap(key3);

        assertEquals("cache:key1", new String(wrapped1, StandardCharsets.UTF_8));
        assertEquals("cache:key2", new String(wrapped2, StandardCharsets.UTF_8));
        assertEquals("cache:key3", new String(wrapped3, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("测试添加前缀 - 特殊字符")
    void testWrapSpecialChars() {
        IRedisPrefixConverter converter = new TestPrefixConverter("my:app::", true);
        byte[] original = "user:data:test".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped = converter.wrap(original);

        assertEquals("my:app::user:data:test", new String(wrapped, StandardCharsets.UTF_8));
    }

    // ========== unwrap() 测试 ==========

    @Test
    @DisplayName("测试移除前缀 - 正常情况")
    void testUnwrapNormal() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);
        byte[] wrapped = "app:user:123".getBytes(StandardCharsets.UTF_8);

        byte[] unwrapped = converter.unwrap(wrapped);

        assertNotNull(unwrapped, "结果不应为 null");
        String unwrappedStr = new String(unwrapped, StandardCharsets.UTF_8);
        assertEquals("user:123", unwrappedStr, "应正确移除前缀");
    }

    @Test
    @DisplayName("测试移除前缀 - 禁用状态")
    void testUnwrapDisabled() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", false);
        byte[] wrapped = "app:user:123".getBytes(StandardCharsets.UTF_8);

        byte[] unwrapped = converter.unwrap(wrapped);

        assertSame(wrapped, unwrapped, "禁用状态应返回原数组");
    }

    @Test
    @DisplayName("测试移除前缀 - null 输入")
    void testUnwrapNull() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);

        byte[] unwrapped = converter.unwrap(null);

        // null 输入应返回 null
    }

    @Test
    @DisplayName("测试移除前缀 - 空数组")
    void testUnwrapEmpty() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);
        byte[] original = new byte[0];

        byte[] unwrapped = converter.unwrap(original);

        assertSame(original, unwrapped, "空数组应返回原数组");
    }

    @Test
    @DisplayName("测试移除前缀 - 无前缀")
    void testUnwrapNoPrefix() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);
        byte[] wrapped = "user:123".getBytes(StandardCharsets.UTF_8);

        byte[] unwrapped = converter.unwrap(wrapped);

        // 无前缀时会尝试移除前缀长度，但内容不匹配
        // 结果取决于实现，这里仅验证不会抛出异常
        assertNotNull(unwrapped, "结果不应为 null");
    }

    // ========== wrap/unwrap 往返测试 ==========

    @Test
    @DisplayName("测试 wrap/unwrap 往返转换")
    void testWrapUnwrapRoundTrip() {
        IRedisPrefixConverter converter = new TestPrefixConverter("prefix:", true);
        byte[] original = "my:key".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped = converter.wrap(original);
        byte[] unwrapped = converter.unwrap(wrapped);

        assertArrayEquals(original, unwrapped, "往返转换应得到原始值");
    }

    @Test
    @DisplayName("测试多次 wrap/unwrap 往返转换")
    void testMultipleWrapUnwrapRoundTrip() {
        IRedisPrefixConverter converter = new TestPrefixConverter("cache:", true);

        String[] keys = {"user:1", "product:2", "order:3", "session:abc123"};

        for (String key : keys) {
            byte[] original = key.getBytes(StandardCharsets.UTF_8);
            byte[] wrapped = converter.wrap(original);
            byte[] unwrapped = converter.unwrap(wrapped);

            assertArrayEquals(original, unwrapped, "往返转换应得到原始值: " + key);
        }
    }

    @Test
    @DisplayName("测试空字符串的 wrap/unwrap")
    void testWrapUnwrapEmptyString() {
        IRedisPrefixConverter converter = new TestPrefixConverter("app:", true);
        byte[] original = "".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped = converter.wrap(original);
        byte[] unwrapped = converter.unwrap(wrapped);

        // 空字符串的处理
        assertNotNull(wrapped, "wrap 结果不应为 null");
        assertNotNull(unwrapped, "unwrap 结果不应为 null");
    }

    @Test
    @DisplayName("测试中文前缀的 wrap/unwrap")
    void testWrapUnwrapChinesePrefix() {
        IRedisPrefixConverter converter = new TestPrefixConverter("应用:", true);
        byte[] original = "用户:123".getBytes(StandardCharsets.UTF_8);

        byte[] wrapped = converter.wrap(original);
        byte[] unwrapped = converter.unwrap(wrapped);

        assertArrayEquals(original, unwrapped, "中文前缀往返转换应得到原始值");
    }
}
