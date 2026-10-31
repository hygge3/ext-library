package ext.library.desensitize.util;

/**
 * 脱敏工具类
 * <p>
 * 提供常用敏感数据的脱敏方法：
 * <ul>
 *   <li>身份证号、银行卡号、手机号</li>
 *   <li>中文姓名、邮箱、地址</li>
 *   <li>密码、IP 地址</li>
 *   <li>自定义滑动打码</li>
 * </ul>
 */
public final class DesensitizeUtil {

    private DesensitizeUtil() {
    }

    /**
     * 银行卡号脱敏，显示前六位后四位
     *
     * <pre>{@code
     * DesensitizeUtil.maskBankCard("62226000000043211234") = "622260**********1234"
     * }</pre>
     *
     * @param input 银行卡号
     * @return 脱敏后的文本
     */
    public static String maskBankCard(String input) {
        if (isEmpty(input)) {
            return input;
        }
        return maskBySlide(input, 6, 4);
    }

    /**
     * 中文姓名脱敏，只显示第一个姓和最后一个字（单名只显示最后一个字）
     *
     * <pre>{@code
     * DesensitizeUtil.maskChineseName("张梦") = "*梦"
     * DesensitizeUtil.maskChineseName("张小梦") = "张*梦"
     * }</pre>
     *
     * @param input 中文姓名
     * @return 脱敏后的文本
     */
    public static String maskChineseName(String input) {
        if (isEmpty(input)) {
            return input;
        }
        return maskBySlide(input, input.length() > 2 ? 1 : 0, 1);
    }

    /**
     * 身份证号脱敏（18位或15位），显示前四位后四位
     *
     * <pre>{@code
     * DesensitizeUtil.maskIdCard("43012319990101432X") = "4301**********432X"
     * DesensitizeUtil.maskIdCard("430123199901432") = "4301*******9432"
     * }</pre>
     * <p>
     * 长度不是 15/18 时保守打码（只保留前两位），不会返回明文
     *
     * @param input 身份证号
     * @return 脱敏后的文本
     */
    public static String maskIdCard(String input) {
        if (isEmpty(input)) {
            return input;
        }
        int length = input.length();
        // 18 位（新证）与 15 位（旧证）均保留前四位后四位
        if (length == 18 || length == 15) {
            return maskBySlide(input, 4, 4);
        }
        // 长度不匹配时保守打码（保留前 2 位），不返回明文
        return maskBySlide(input, Math.min(2, length), 0);
    }

    /**
     * 手机号脱敏，显示前三位后四位（+86 前缀保留）
     *
     * <pre>{@code
     * DesensitizeUtil.maskPhone("13812345678") = "138****5678"
     * DesensitizeUtil.maskPhone("+8613812345678") = "+861****5678"
     * }</pre>
     *
     * @param input 手机号
     * @return 脱敏后的文本
     */
    public static String maskPhone(String input) {
        if (isEmpty(input)) {
            return input;
        }
        int digits = countDigits(input);
        // 带 +86 前缀共 13 位数字，保留前缀与前三位
        int head = digits == 13 ? 5 : 3;
        return maskBySlide(input, head, 4);
    }

    /**
     * 统计字符串中的数字个数
     *
     * @param text 输入字符串
     * @return 数字个数
     */
    private static int countDigits(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i))) {
                count++;
            }
        }
        return count;
    }

    /**
     * 地址脱敏，只显示前六个字符（通常到区/县级）
     *
     * <pre>{@code
     * DesensitizeUtil.maskAddress("北京市西城区金城坊街2号") = "北京市西城区******"
     * }</pre>
     *
     * @param input 地址
     * @return 脱敏后的文本
     */
    public static String maskAddress(String input) {
        if (isEmpty(input)) {
            return input;
        }
        return maskBySlide(input, 6, 0);
    }

    /**
     * 邮箱脱敏，@ 前的用户名只保留首字符，域名完整保留
     *
     * <pre>{@code
     * DesensitizeUtil.maskEmail("test.demo@qq.com") = "t********@qq.com"
     * }</pre>
     *
     * @param input 邮箱地址
     * @return 脱敏后的文本
     */
    public static String maskEmail(String input) {
        if (isEmpty(input)) {
            return input;
        }
        int atIndex = input.indexOf('@');
        if (atIndex <= 0) {
            // 非邮箱格式，保守打码
            return maskBySlide(input, 1, 0);
        }
        return maskBySlide(input.substring(0, atIndex), 1, 0) + input.substring(atIndex);
    }

    /**
     * 密码脱敏，统一显示为固定掩码
     *
     * <pre>{@code
     * DesensitizeUtil.maskPassword("any_password") = "******"
     * }</pre>
     *
     * @param input 密码（忽略）
     * @return 固定掩码 "******"
     */
    public static String maskPassword(String input) {
        return "******";
    }

    /**
     * IP 地址脱敏，支持 IPv4 和 IPv6
     *
     * <pre>{@code
     * DesensitizeUtil.maskIp("192.168.2.1") = "192.*.*.*"
     * DesensitizeUtil.maskIp("2001:0db8:02de:0000:0000:0000:0000:0e13") = "2001:*:*:*:*:*:*:*"
     * }</pre>
     *
     * @param input IP 地址
     * @return 脱敏后的文本
     */
    public static String maskIp(String input) {
        if (input == null) {
            return null;
        }
        // IPv4
        int dotIndex = input.indexOf('.');
        if (dotIndex > 0) {
            return input.substring(0, dotIndex) + ".*.*.*";
        }
        // IPv6
        int colonIndex = input.indexOf(':');
        if (colonIndex > 0) {
            return input.substring(0, colonIndex) + ":*:*:*:*:*:*:*";
        }
        // 无法识别为 IP（主机名、空串等），全掩码，绝不返回明文
        return "*".repeat(input.length());
    }

    /**
     * 滑动打码，保留头部和尾部指定长度，中间用 * 替换
     *
     * <pre>{@code
     * DesensitizeUtil.maskBySlide("Hello World", 2, 3) = "He******rld"
     * }</pre>
     *
     * @param input 输入字符串
     * @param head  头部保留长度
     * @param tail  尾部保留长度
     * @return 脱敏后的文本
     */
    public static String maskBySlide(String input, int head, int tail) {
        return maskBySlide(input, head, tail, "*");
    }

    /**
     * 滑动打码，保留头部和尾部指定长度，中间用指定字符替换
     *
     * <pre>{@code
     * DesensitizeUtil.maskBySlide("Hello World", 2, 3, "#") = "He######rld"
     * }</pre>
     *
     * <p>
     * 输入短到无法同时保留头尾时（{@code head + tail >= length}），退化为等长全掩码，<b>绝不返回明文</b>
     *
     * @param input      输入字符串
     * @param head       头部保留长度
     * @param tail       尾部保留长度
     * @param maskString 替换字符
     * @return 脱敏后的文本
     */
    public static String maskBySlide(String input, int head, int tail, String maskString) {
        if (isEmpty(input)) {
            return input;
        }
        int length = input.length();
        if (head + tail >= length) {
            return maskString.repeat(length);
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (i < head || i >= length - tail) {
                sb.append(input.charAt(i));
            } else {
                sb.append(maskString);
            }
        }
        return sb.toString();
    }

    /**
     * 反向滑动打码，隐藏头部和尾部，显示中间部分
     *
     * <pre>{@code
     * DesensitizeUtil.maskBySlideReverse("Hello World", 2, 3) = "**llo Wo***"
     * }</pre>
     *
     * @param input 输入字符串
     * @param head  头部隐藏长度
     * @param tail  尾部隐藏长度
     * @return 脱敏后的文本
     */
    public static String maskBySlideReverse(String input, int head, int tail) {
        return maskBySlideReverse(input, head, tail, "*");
    }

    /**
     * 反向滑动打码，隐藏头部和尾部，显示中间部分
     *
     * @param input      输入字符串
     * @param head       头部隐藏长度
     * @param tail       尾部隐藏长度
     * @param maskString 替换字符
     * @return 脱敏后的文本
     */
    public static String maskBySlideReverse(String input, int head, int tail, String maskString) {
        if (isEmpty(input)) {
            return input;
        }
        int length = input.length();
        if (head + tail >= length) {
            return maskString.repeat(length);
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            if (i < head || i >= length - tail) {
                sb.append(maskString);
            } else {
                sb.append(input.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * 通用脱敏（{@link #maskBySlide(String, int, int)} 的三分之一版本），保留前 1/3 和后 1/3，中间掩码
     * <p>
     * 用于没有专用规则的敏感字段，按字符计数（一个汉字算 1 位），输出长度与输入一致。
     * <pre>{@code
     * DesensitizeUtil.maskGeneric("13812345678") = "1381***5678"
     * DesensitizeUtil.maskGeneric("北京市海淀区中关村大街1号") = "北京市海淀***村大街1号"
     * }</pre>
     * <p>
     * 输入短到无法同时保留头尾时（2 位、4 位等，此时前后各占一半），退化为等长全掩码，绝不返回明文。
     *
     * @param input 输入字符串
     * @return 脱敏后的文本
     */
    public static String maskGeneric(String input) {
        if (isEmpty(input)) {
            return input;
        }
        int length = input.length();
        // ceil(length / 3)，避开 length=1 时 head+tail >= length 导致的边界问题
        int keep = length <= 1 ? 0 : (length + 2) / 3;
        return maskBySlide(input, keep, keep);
    }

    private static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }
}
