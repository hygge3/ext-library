package ext.library.core.util;

import ext.library.core.constant.PatternPool;
import ext.library.tool.$;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

/**
 * Validator 校验框架工具
 */
@UtilityClass
public class ValidatorUtil {

    /**
     * 通过正则表达式验证
     *
     * @param pattern 正则模式
     * @param value   值
     * @return 是否匹配正则
     */
    public boolean isMatchRegex(final Pattern pattern, final CharSequence value) {
        if (value == null || pattern == null) {
            // 提供 null 的字符串为不匹配
            return false;
        }
        return pattern.matcher(value).matches();
    }

    /**
     * 验证是否为手机号码（中国）
     *
     * @param value 值
     * @return 是否为手机号码（中国）
     */
    public boolean isMobile(final CharSequence value) {
        return isMatchRegex(PatternPool.MOBILE, value);
    }

    /**
     * 验证是否都为汉字
     *
     * @param value 值
     * @return 是否为汉字
     */
    public boolean isChinese(final CharSequence value) {
        return isMatchRegex(PatternPool.CHINESES, value);
    }

    /**
     * 验证该字符串是否是字母（包括大写和小写字母）
     *
     * @param value 字符串内容
     * @return 是否是字母（包括大写和小写字母）
     * @since 4.1.8
     */
    public boolean isWord(final CharSequence value) {
        return isMatchRegex(PatternPool.WORD, value);
    }

    /**
     * 验证是否包含 Xss 攻击
     *
     * @param value 值
     * @return 是否为 UUID
     */
    public boolean hasXss(final CharSequence value) {
        if ($.isEmpty(value)) {
            return false;
        }
        return PatternPool.RE_HTML_MARK.matcher(value).find();
    }

    /**
     * 验证是否为邮政编码（中国）
     *
     * @param value 值
     * @return 是否为邮政编码（中国）
     */
    public boolean isZipCode(final CharSequence value) {
        return isMatchRegex(PatternPool.ZIP_CODE, value);
    }

}
