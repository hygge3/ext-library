package ext.library.desensitize.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DesensitizeUtil 测试
 */
@DisplayName("DesensitizeUtil 脱敏工具测试")
class DesensitizeUtilTest {

    // ========== 通用 ==========

    @Test
    @DisplayName("通用脱敏保留前 1/3 和后 1/3")
    void testMaskGeneric() {
        // 11 位手机号：ceil(11/3)=4，前后各留 4，中间 3 位掩码
        assertEquals("1381***5678", DesensitizeUtil.maskGeneric("13812345678"));
        // 汉字按字符计数，一个汉字算 1 位：13 字 -> 前后各留 5 字
        assertEquals("北京市海淀***村大街1号",
                DesensitizeUtil.maskGeneric("北京市海淀区中关村大街1号"));
    }

    @Test
    @DisplayName("通用脱敏对短输入绝不全量明文返回")
    void testMaskGenericNeverLeaks() {
        assertEquals("*", DesensitizeUtil.maskGeneric("x"));
        assertEquals("**", DesensitizeUtil.maskGeneric("ab"));
        assertEquals("a*c", DesensitizeUtil.maskGeneric("abc"));
        // 4 位时前后各留 2 位刚好占满，退化为全掩码而不是明文
        assertEquals("****", DesensitizeUtil.maskGeneric("abcd"));
        assertEquals("ab*de", DesensitizeUtil.maskGeneric("abcde"));
        assertEquals("", DesensitizeUtil.maskGeneric(""));
        assertNull(DesensitizeUtil.maskGeneric(null));
    }

    @Test
    @DisplayName("通用脱敏输出长度与输入一致")
    void testMaskGenericKeepsLength() {
        for (String s : new String[]{"a", "ab", "abc", "abcd", "abcde", "1234567890", "北京市海淀区"}) {
            assertEquals(s.length(), DesensitizeUtil.maskGeneric(s).length(), "长度应守恒：" + s);
        }
    }

    // ========== 身份证 ==========

    @Test
    @DisplayName("18 位身份证脱敏")
    void testMaskIdCard18() {
        assertEquals("4301**********432X", DesensitizeUtil.maskIdCard("43012319990101432X"));
    }

    @Test
    @DisplayName("15 位旧身份证也必须脱敏")
    void testMaskIdCard15() {
        String masked = DesensitizeUtil.maskIdCard("430123199901432");
        assertTrue(masked.contains("*"), "15 位身份证必须打码，不能返回明文");
        assertTrue(masked.startsWith("4301"), "应保留前四位");
        assertTrue(masked.endsWith("1432"), "应保留后四位");
    }

    @Test
    @DisplayName("长度非法的身份证保守打码，不返回明文")
    void testMaskIdCardInvalidLength() {
        String masked = DesensitizeUtil.maskIdCard("12345");
        assertTrue(masked.contains("*"), "长度异常也必须打码");
    }

    @Test
    @DisplayName("空值身份证")
    void testMaskIdCardEmpty() {
        assertNull(DesensitizeUtil.maskIdCard(null));
        assertEquals("", DesensitizeUtil.maskIdCard(""));
    }

    // ========== 手机号 ==========

    @Test
    @DisplayName("11 位手机号脱敏")
    void testMaskPhone() {
        assertEquals("138****5678", DesensitizeUtil.maskPhone("13812345678"));
    }

    @Test
    @DisplayName("带 +86 前缀手机号脱敏")
    void testMaskPhoneWithPrefix() {
        String masked = DesensitizeUtil.maskPhone("+8613812345678");
        assertTrue(masked.startsWith("+8613"), "应保留 +861 前缀与前三位");
        assertTrue(masked.endsWith("5678"), "应保留后四位");
        assertTrue(masked.contains("*"), "必须打码");
    }

    @Test
    @DisplayName("座机号码脱敏")
    void testMaskPhoneLandline() {
        String masked = DesensitizeUtil.maskPhone("010-87654321");
        assertTrue(masked.contains("*"), "座机也必须打码，不能返回明文");
    }

    @Test
    @DisplayName("空值手机号")
    void testMaskPhoneEmpty() {
        assertNull(DesensitizeUtil.maskPhone(null));
    }

    // ========== 邮箱 ==========

    @Test
    @DisplayName("邮箱脱敏")
    void testMaskEmail() {
        assertEquals("t********@qq.com", DesensitizeUtil.maskEmail("test.demo@qq.com"));
    }

    @Test
    @DisplayName("非邮箱格式保守打码")
    void testMaskEmailInvalid() {
        String masked = DesensitizeUtil.maskEmail("not-an-email");
        assertTrue(masked.contains("*"), "非邮箱也必须打码");
    }

    // ========== 其它 ==========

    @Test
    @DisplayName("银行卡脱敏")
    void testMaskBankCard() {
        assertEquals("622260**********1234", DesensitizeUtil.maskBankCard("62226000000043211234"));
    }

    @Test
    @DisplayName("中文姓名脱敏")
    void testMaskChineseName() {
        assertEquals("张*梦", DesensitizeUtil.maskChineseName("张小梦"));
        assertEquals("*梦", DesensitizeUtil.maskChineseName("张梦"));
    }

    @Test
    @DisplayName("地址脱敏")
    void testMaskAddress() {
        assertEquals("北京市西城区******", DesensitizeUtil.maskAddress("北京市西城区金城坊街2号"));
    }

    @Test
    @DisplayName("密码脱敏为固定掩码")
    void testMaskPassword() {
        assertEquals("******", DesensitizeUtil.maskPassword("any"));
        assertEquals("******", DesensitizeUtil.maskPassword(null));
    }

    @Test
    @DisplayName("IPv4 / IPv6 脱敏")
    void testMaskIp() {
        assertEquals("192.*.*.*", DesensitizeUtil.maskIp("192.168.2.1"));
        assertEquals("2001:*:*:*:*:*:*:*", DesensitizeUtil.maskIp("2001:0db8:02de:0000:0000:0000:0000:0e13"));
        assertNull(DesensitizeUtil.maskIp(null));
    }

    @Test
    @DisplayName("滑动打码")
    void testMaskBySlide() {
        assertEquals("He******rld", DesensitizeUtil.maskBySlide("Hello World", 2, 3));
        assertEquals("He######rld", DesensitizeUtil.maskBySlide("Hello World", 2, 3, "#"));
    }

    @Test
    @DisplayName("反向滑动打码")
    void testMaskBySlideReverse() {
        assertEquals("**llo Wo***", DesensitizeUtil.maskBySlideReverse("Hello World", 2, 3));
    }

    @Test
    @DisplayName("打码长度覆盖全串时全部掩码")
    void testMaskBySlideOverLength() {
        // 等长全掩码：3 个字符 -> 3 个掩码字符
        assertEquals("***", DesensitizeUtil.maskBySlideReverse("abc", 2, 2));
    }

    // ========== 短输入不得返回明文 ==========

    @Test
    @DisplayName("短输入遮不住时退化为全掩码，绝不返回明文")
    void testMaskBySlideShortInputNeverLeaks() {
        assertEquals("***", DesensitizeUtil.maskBySlide("abc", 2, 2));
        assertEquals("###", DesensitizeUtil.maskBySlide("abc", 2, 2, "#"));
        // 手机号规则 head=3 tail=4，7 位恰好 head+tail>=length
        assertEquals("*******", DesensitizeUtil.maskPhone("1234567"));
        // 身份证长度 1/2 时 Math.min(2, length) == length
        assertEquals("*", DesensitizeUtil.maskIdCard("1"));
        assertEquals("**", DesensitizeUtil.maskIdCard("12"));
        // 邮箱用户名部分只有 1 个字符
        assertEquals("*@x.com", DesensitizeUtil.maskEmail("a@x.com"));
        // 地址短于 6
        assertEquals("**", DesensitizeUtil.maskAddress("北京"));
        // 卡号短于 10
        assertEquals("****", DesensitizeUtil.maskBankCard("1234"));
        // 姓名 1 个字符
        assertEquals("*", DesensitizeUtil.maskChineseName("张"));
    }

    @Test
    @DisplayName("maskBySlide / maskBySlideReverse 短输入行为一致（均为全掩码）")
    void testMaskBySlideConsistentWithReverse() {
        assertEquals(DesensitizeUtil.maskBySlideReverse("abcd", 3, 3),
                DesensitizeUtil.maskBySlide("abcd", 3, 3));
    }

    @Test
    @DisplayName("无法识别的 IP 全掩码，绝不返回明文")
    void testMaskIpUnrecognizedNeverLeaks() {
        assertEquals("*********", DesensitizeUtil.maskIp("localhost"));
        assertEquals("*********", DesensitizeUtil.maskIp("not-an-ip"));
        assertEquals("", DesensitizeUtil.maskIp(""));
        assertNull(DesensitizeUtil.maskIp(null));
    }

}
