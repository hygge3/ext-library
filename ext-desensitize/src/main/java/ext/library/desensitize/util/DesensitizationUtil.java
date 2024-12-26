package ext.library.desensitize.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 脱敏工具类
 * <ul>
 * <li>支持常用类型的脱敏：如姓名、身份证、银行卡号、手机号、密码、加密密文、座机、邮箱、地址、IP 等</li>
 * <li>支持自定义前后保留多少位脱敏，可自定义脱敏占位符</li>
 * <li>支持基于自定义规则的脱敏：如指定"3-6，8，10-"表示第 4，5，6，7，9，11 以及 11 之后的位使用加密字符替换</li>
 * </ul>
 */
@UtilityClass
public final class DesensitizationUtil {

    /**
     * 中文姓名只显示第一个姓和最后一个汉字（单名则只显示最后一个汉字），其他隐藏为星号 <pre>
     *     DesensitizationUtil.maskChineseName("张梦") = "*梦"
     *     DesensitizationUtil.maskChineseName("张小梦") = "张*梦"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    public String maskChineseName(String input) {
        if (isEmptyText(input)) {
            return input;
        }
        return maskBySlide(input, input.length() > 2 ? 1 : 0, 1);
    }

    /**
     * 身份证 (18 位或者 15 位) 显示前六位，四位，其他隐藏。 <pre>
     *     DesensitizationUtil.maskIdCardNo("43012319990101432X") = "430123********432X"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    public String maskIdCardNo(String input) {
        if (isEmptyText(input)) {
            return input;
        }
        return input.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1****$2");
    }

    /**
     * 移动电话前三位，后四位，其他隐藏，比如 <pre>
     * DesensitizationUtil.maskPhoneNumber("13812345678") = "138******10"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    public String maskPhoneNumber(String input) {
        if (isEmptyText(input)) {
            return input;
        }
        return input.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 地址脱敏，只显示到地区，不显示详细地址 <pre>
     * DesensitizationUtil.maskAddress("北京市西城区金城坊街 2 号") = "北京市西城区******"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    public String maskAddress(String input) {
        if (isEmptyText(input)) {
            return input;
        }
        return maskBySlide(input, 6, 0);
    }

    /**
     * 电子邮箱脱敏，邮箱前缀最多显示前 1 字母，前缀其他隐藏，用星号代替，@及后面的地址显示 <pre>
     * DesensitizationUtil.maskEmail("test.demo@qq.com") = "t****@qq.com"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    public String maskEmail(String input) {
        if (isEmptyText(input)) {
            return input;
        }
        return input.replaceAll("(^\\\\w)[^@]*(@.*$)", "$1****$2");
    }

    /**
     * 银行卡号脱敏，显示前六位后四位 <pre>
     * DesensitizationUtil.maskBankCardNo("62226000000043211234") = "622260**********1234"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    public String maskBankCardNo(String input) {
        if (isEmptyText(input)) {
            return input;
        }
        return maskBySlide(input, 6, 4);
    }

    /**
     * 密码脱敏，用******代替 <pre>
     * DesensitizationUtil.maskPassword(password) = "******"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    @NotNull
    @Contract(pure = true)
    public String maskPassword(String input) {
        return "******";
    }

    /**
     * IPv 脱敏，支持 IPv4 和 IPv6 <pre>
     * DesensitizationUtil.maskIP("192.168.2.1") = "192.*.*.*"
     * DesensitizationUtil.maskIP("2001:0db8:02de:0000:0000:0000:0000:0e13") = "2001:*:*:*:*:*:*:*"
     * DesensitizationUtil.maskIP("2001:db8:2de:0000:0000:0000:0000:e13") = "2001:*:*:*:*:*:*:*"
     * DesensitizationUtil.maskIP("2001:db8:2de:0:0:0:0:e13") = "2001:*:*:*:*:*:*:*"
     * </pre>
     *
     * @param input 待处理的文本
     * @return 屏蔽后的文本
     */
    @Contract("null->null")
    public String maskIp(String input) {
        if (null == input) {
            return null;
        }
        int index = input.indexOf(".");
        if (index > 0) {
            return input.substring(0, index) + ".*.*.*";
        }
        index = input.indexOf(":");
        if (index > 0) {
            return input.substring(0, index) + ":*:*:*:*:*:*:*";
        }
        return input;
    }

    /**
     * 滑动打码。
     *
     * <pre>
     *     DesensitizationUtil.maskBySlide("Hello World", 2, 3) = "He******rld"
     * </pre>
     *
     * @param input 输入字符串
     * @param head  头部保留长度
     * @param tail  尾部保留长度
     * @return 屏蔽后的文本
     */
    public String maskBySlide(String input, int head, int tail) {
        return maskBySlide(input, head, tail, false);
    }

    /**
     * 滑动打码。
     *
     * <pre>
     *     DesensitizationUtil.maskBySlide("Hello World", 2, 3) = "He******rld"
     * </pre>
     *
     * @param input   输入字符串
     * @param head    头部保留长度
     * @param tail    尾部保留长度
     * @param reverse 是否反转
     * @return 屏蔽后的文本
     */
    public String maskBySlide(String input, int head, int tail, boolean reverse) {
        return maskBySlide(input, head, tail, "*", reverse);
    }

    /**
     * 滑动打码。 <pre>
     * DesensitizationUtil.maskBySlide("Hello World", 2, 3, "#") = "He######rld"
     * </pre>
     *
     * @param input      输入字符串
     * @param head       头部保留长度
     * @param tail       尾部保留长度
     * @param maskString 替换结果字符
     * @return 屏蔽后的文本
     */
    public String maskBySlide(String input, int head, int tail, String maskString) {
        return maskBySlide(input, head, tail, maskString, false);
    }

    /**
     * 滑动打码。 <pre>
     * DesensitizationUtil.maskBySlide("Hello World", 2, 3, "#") = "He######rld"
     * </pre>
     *
     * @param input      输入字符串
     * @param head       头部保留长度
     * @param tail       尾部保留长度
     * @param maskString 替换结果字符
     * @return 屏蔽后的文本
     */
    public String maskBySlide(String input, int head, int tail, String maskString, boolean reverse) {
        if (isEmptyText(input)) {
            return input;
        }
        if (head + tail >= input.length()) {
            return input;
        }
        StringBuilder sb = new StringBuilder();

        char[] chars = input.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; i++) {
            // 明文位内则明文显示
            if (i < head || i > (length - tail - 1)) {
                sb.append(reverse ? maskString : chars[i]);
            } else {
                sb.append(reverse ? chars[i] : maskString);
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否无效字符串
     *
     * @param text 字符串
     * @return true-无效字符串
     */
    @Contract(value = "null->true", pure = true)
    private boolean isEmptyText(String text) {
        return null == text || text.isEmpty();
    }

}
