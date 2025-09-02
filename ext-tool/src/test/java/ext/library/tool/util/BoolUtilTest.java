package ext.library.tool.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BoolUtil 工具类测试")
class BoolUtilTest {

    @Test
    @DisplayName("测试字符串类型的真值判断")
    void testIsTrueWithString() {
        // 测试预定义的真值字符串
        assertTrue(BoolUtil.isTrue("1"), "字符串'1'应该返回 true");
        assertTrue(BoolUtil.isTrue("true"), "字符串'true'应该返回 true");
        assertTrue(BoolUtil.isTrue("yes"), "字符串'yes'应该返回 true");
        assertTrue(BoolUtil.isTrue("ok"), "字符串'ok'应该返回 true");
        assertTrue(BoolUtil.isTrue("y"), "字符串'y'应该返回 true");

        // 测试预定义的假值字符串
        assertFalse(BoolUtil.isTrue("0"), "字符串'0'应该返回 false");
        assertFalse(BoolUtil.isTrue("false"), "字符串'false'应该返回 false");
        assertFalse(BoolUtil.isTrue("no"), "字符串'no'应该返回 false");
        assertFalse(BoolUtil.isTrue("n"), "字符串'n'应该返回 false");

        // 测试不在预定义集合中的字符串
        assertFalse(BoolUtil.isTrue("maybe"), "不在预定义真值集合中的字符串应该返回 false");
        assertFalse(BoolUtil.isTrue("unknown"), "不在预定义真值集合中的字符串应该返回 false");
    }

    @Test
    @DisplayName("测试数字类型的真值判断")
    void testIsTrueWithNumbers() {
        // 测试大于 0 的数字
        assertTrue(BoolUtil.isTrue(1), "正整数应该返回 true");
        assertTrue(BoolUtil.isTrue(1.0), "正小数应该返回 true");
        assertTrue(BoolUtil.isTrue(100L), "正长整型应该返回 true");

        // 测试等于 0 的数字
        assertFalse(BoolUtil.isTrue(0), "零应该返回 false");
        assertFalse(BoolUtil.isTrue(0.0), "零点零应该返回 false");

        // 测试小于 0 的数字
        assertFalse(BoolUtil.isTrue(-1), "负数应该返回 false");
        assertFalse(BoolUtil.isTrue(-0.1), "负小数应该返回 false");
    }

    @Test
    @DisplayName("测试布尔类型的真值判断")
    void testIsTrueWithBoolean() {
        assertTrue(BoolUtil.isTrue(true), "布尔值 true 应该返回 true");
        assertFalse(BoolUtil.isTrue(false), "布尔值 false 应该返回 false");
    }

    @Test
    @DisplayName("测试其他类型的真值判断")
    void testIsTrueWithOtherTypes() {
        // 测试其他类型的对象
        assertFalse(BoolUtil.isTrue(new Object()), "普通对象应该返回 false");
        assertFalse(BoolUtil.isTrue(null), "null 值应该返回 false");
        assertFalse(BoolUtil.isTrue(new int[]{1, 2, 3}), "数组对象应该返回 false");
    }

    @Test
    @DisplayName("测试字符串类型的假值判断")
    void testIsFalseWithString() {
        // 测试预定义的假值字符串
        assertTrue(BoolUtil.isFalse("0"), "字符串'0'应该返回 true");
        assertTrue(BoolUtil.isFalse("false"), "字符串'false'应该返回 true");
        assertTrue(BoolUtil.isFalse("no"), "字符串'no'应该返回 true");
        assertTrue(BoolUtil.isFalse("n"), "字符串'n'应该返回 true");

        // 测试预定义的真值字符串
        assertFalse(BoolUtil.isFalse("1"), "字符串'1'应该返回 false");
        assertFalse(BoolUtil.isFalse("true"), "字符串'true'应该返回 false");
        assertFalse(BoolUtil.isFalse("yes"), "字符串'yes'应该返回 false");
        assertFalse(BoolUtil.isFalse("ok"), "字符串'ok'应该返回 false");
        assertFalse(BoolUtil.isFalse("y"), "字符串'y'应该返回 false");

        // 测试不在预定义集合中的字符串
        assertFalse(BoolUtil.isFalse("maybe"), "不在预定义假值集合中的字符串应该返回 false");
        assertFalse(BoolUtil.isFalse("unknown"), "不在预定义假值集合中的字符串应该返回 false");
    }

    @Test
    @DisplayName("测试数字类型的假值判断")
    void testIsFalseWithNumbers() {
        // 测试大于 0 的数字
        assertFalse(BoolUtil.isFalse(1), "正整数应该返回 false");
        assertFalse(BoolUtil.isFalse(1.0), "正小数应该返回 false");
        assertFalse(BoolUtil.isFalse(100L), "正长整型应该返回 false");

        // 测试等于 0 的数字
        assertTrue(BoolUtil.isFalse(0), "零应该返回 true");
        assertTrue(BoolUtil.isFalse(0.0), "零点零应该返回 true");

        // 测试小于 0 的数字
        assertTrue(BoolUtil.isFalse(-1), "负数应该返回 true");
        assertTrue(BoolUtil.isFalse(-0.1), "负小数应该返回 true");
    }

    @Test
    @DisplayName("测试布尔类型的假值判断")
    void testIsFalseWithBoolean() {
        assertFalse(BoolUtil.isFalse(true), "布尔值 true 应该返回 false");
        assertTrue(BoolUtil.isFalse(false), "布尔值 false 应该返回 true");
    }

    @Test
    @DisplayName("测试 null 值和其他类型的假值判断")
    void testIsFalseWithOtherTypes() {
        // 测试 null 值
        assertFalse(BoolUtil.isFalse(null), "null 值应该返回 false");

        // 测试其他类型的对象
        assertFalse(BoolUtil.isFalse(new Object()), "普通对象应该返回 false");
        assertFalse(BoolUtil.isFalse(new int[]{1, 2, 3}), "数组对象应该返回 false");
    }
}