package ext.library.tool.util;

import ext.library.tool.constant.PatternPool;

import java.util.regex.Pattern;

/**
 * Validator 校验框架工具
 */
public final class ValidatorUtil {

    private ValidatorUtil() {
    }

    /**
     * 通过正则表达式验证
     *
     * @param pattern 正则模式
     * @param value   值
     *
     * @return 是否匹配正则
     */
    public static boolean isMatchRegex(Pattern pattern, CharSequence value) {
        if (pattern == null || value == null) {
            return false;
        }
        return pattern.matcher(value).matches();
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     *
     * @return 是否为手机号码（中国）
     */
    public static boolean isMobile(CharSequence value) {
        return isMatchRegex(PatternPool.MOBILE, value);
    }

    /**
     * 验证是否都为汉字
     *
     * @param value 值
     *
     * @return 是否为汉字
     */
    public static boolean isChinese(CharSequence value) {
        return isMatchRegex(PatternPool.CHINESE, value);
    }

    /**
     * 验证该字符串是否是字母（包括大写和小写字母）
     *
     * @param value 字符串内容
     *
     * @return 是否是字母（包括大写和小写字母）
     */
    public static boolean isWord(CharSequence value) {
        return isMatchRegex(PatternPool.WORD, value);
    }

    /**
     * 验证是否包含 XSS 攻击
     *
     * @param value 值
     *
     * @return 是否包含 XSS 攻击
     */
    public static boolean hasXss(CharSequence value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return PatternPool.HTML_TAG.matcher(value).find();
    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param value 值
     *
     * @return 是否为邮政编码（中国）
     */
    public static boolean isZipCode(CharSequence value) {
        return isMatchRegex(PatternPool.ZIP_CODE, value);
    }

}
