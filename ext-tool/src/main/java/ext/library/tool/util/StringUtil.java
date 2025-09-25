package ext.library.tool.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.html.HtmlEscapers;
import ext.library.tool.constant.Symbol;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class StringUtil {

    /**
     * 首字母变小写
     *
     * @param str 字符串
     *
     * @return {String}
     */
    public static String firstCharToLower(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] chars = str.toCharArray();
            chars[0] -= ('a' - 'A');
            return new String(chars);
        }
        return str;
    }

    /**
     * 首字母变大写
     *
     * @param str 字符串
     *
     * @return {String}
     */
    public static String firstCharToUpper(String str) {
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] chars = str.toCharArray();
            chars[0] += ('a' - 'A');
            return new String(chars);
        }
        return str;
    }

    /**
     * 将下划线转换为 HUMP 格式
     * <pre>
     * StringUtils.lowerCaseFirst(null, *)             = null
     * StringUtils.lowerCaseFirst("", *)               = ""
     * StringUtils.lowerCaseFirst("aaa_bbb", *)        = "aaaBbb"
     * </pre>
     *
     * @param underscoreText 下划线文本
     *
     * @return 驼峰文本
     */
    public static String underlineToCamelCase(String underscoreText) {
        if (isBlank(underscoreText)) {
            return underscoreText;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (int i = 0; i < underscoreText.length(); i++) {
            char ch = underscoreText.charAt(i);
            if ('_' == ch) {
                flag = true;
            } else {
                if (flag) {
                    result.append(Character.toUpperCase(ch));
                    flag = false;
                } else {
                    result.append(ch);
                }
            }
        }
        return result.toString();
    }

    /**
     * is empty
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * @param str str
     *
     * @return boolean
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * is not empty
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str str
     *
     * @return boolean
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }


    /**
     * 判断是否为空字符串 <pre class="code">
     * $.isBlank(null)		= true
     * $.isBlank("")		= true
     * $.isBlank(" ")		= true
     * $.isBlank("12345")	= false
     * $.isBlank(" 12345 ")	= false
     * </pre>
     *
     * @param str the {@code String} to check (maybe {@code null})
     *
     * @return {@code true} if the {@code CharSequence} is not {@code null}, its length is
     * greater than 0, and it does not contain whitespace only
     *
     * @see Character#isWhitespace
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        return str.isBlank();
    }

    /**
     * 判断不为空字符串 <pre>
     * $.isNotBlank(null)	= false
     * $.isNotBlank("")		= false
     * $.isNotBlank(" ")	= false
     * $.isNotBlank("bob")	= true
     * $.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     *
     * @return {@code true} if the CharSequence is not empty and not null and not
     * whitespace
     *
     * @see Character#isWhitespace
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断是否有任意一个 空字符串
     *
     * @param strs String
     *
     * @return boolean
     */
    public static boolean isAnyBlank(String... strs) {
        if (null == strs || Array.getLength(strs) == 0) {
            return true;
        }
        return Stream.of(strs).anyMatch(StringUtil::isBlank);
    }

    /**
     * 有 任意 一个 Blank
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public static boolean isAnyBlank(Collection<String> strs) {
        if (null == strs || strs.isEmpty()) {
            return true;
        }
        return strs.stream().anyMatch(StringUtil::isBlank);
    }

    /**
     * 判断是否全为非空字符串
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public static boolean isNoneBlank(String... strs) {
        return !isAnyBlank(strs);
    }

    /**
     * 判断是否全为非空字符串
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public static boolean isNoneBlank(Collection<String> strs) {
        return !isAnyBlank(strs);
    }

    /**
     * 有任意一个非空
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public static boolean isAnyNotBlank(String... strs) {
        if (null == strs || Array.getLength(strs) == 0) {
            return false;
        }
        return Stream.of(strs).anyMatch(StringUtil::isNotBlank);
    }

    /**
     * 是否以指定字符串开头
     *
     * @param str    被检测字符串
     * @param prefix 开头字符串
     *
     * @return {boolean}
     */
    public static boolean startWith(String str, String prefix) {
        if (isEmpty(str)) {
            return false;
        }
        return str.startsWith(prefix);
    }

    /**
     * startWith char
     *
     * @param cs CharSequence
     * @param c  char
     *
     * @return {boolean}
     */
    public static boolean startWith(CharSequence cs, char c) {
        return cs.charAt(0) == c;
    }

    /**
     * endWith char
     *
     * @param str    被检测字符串
     * @param prefix 结尾字符串
     *
     * @return {boolean}
     */
    public static boolean endWith(String str, String prefix) {
        if (isEmpty(str)) {
            return false;
        }
        return str.endsWith(prefix);
    }

    /**
     * endWith char
     *
     * @param cs CharSequence
     * @param c  char
     *
     * @return {boolean}
     */
    public static boolean endWith(CharSequence cs, char c) {
        return cs.charAt(cs.length() - 1) == c;
    }

    /**
     * 将字符串中特定模式的字符转换成 map 中对应的值
     * <p>
     * use: format("my name is ${name}, and i like ${like}!", {"name":"L.cm", "like":
     * "Java"})
     *
     * @param message 需要转换的字符串
     * @param params  转换所需的键值对集合
     *
     * @return 转换后的字符串
     */
    public static String format(String message, Map<String, ?> params) {
        // message 为 null 返回空字符串
        if (message == null) {
            return Symbol.EMPTY;
        }
        // 参数为 null 或者为空
        if (params == null || params.isEmpty()) {
            return message;
        }
        // 使用正则表达式匹配占位符
        Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(message);
        StringBuilder sb = new StringBuilder((int) (message.length() * 1.5));
        int lastEnd = 0;
        while (matcher.find()) {
            sb.append(message, lastEnd, matcher.start());
            String key = matcher.group(1).trim();
            Object value = params.get(key);
            if (value == null) {
                // 处理无效占位符，可以选择保留原样或者替换为空字符串
                sb.append(matcher.group(0));
            } else {
                sb.append(value);
            }
            lastEnd = matcher.end();
        }
        sb.append(message.substring(lastEnd));
        return sb.toString();
    }

    /**
     * 同 log 格式的 format 规则
     * <p>
     * use: format("my name is {}, and i like {}!", "L.cm", "Java")
     * <p>
     * 注意：
     * 在循环结束后检查是否还有未匹配的 {}，如果有则抛出 IllegalArgumentException
     * 如果 arguments 数组长度小于 message 中的占位符数量，抛出 IllegalArgumentException
     *
     * @param message   需要转换的字符串
     * @param arguments 需要替换的变量
     *
     * @return 转换后的字符串
     */
    public static String format(String message, Object... arguments) {
        // message 为 null 返回空字符串
        if (message == null) {
            return Symbol.EMPTY;
        }
        // 参数为 null 或者为空
        if (arguments == null || arguments.length == 0) {
            return message;
        }

        // 使用正则表达式匹配所有的 {}
        Pattern pattern = Pattern.compile("\\{\\}");
        Matcher matcher = pattern.matcher(message);

        // 初始化 StringBuilder，预期转换后的字符串长度为原长度的 1.5 倍
        StringBuilder sb = new StringBuilder((int) (message.length() * 1.5));

        int index = 0;
        int lastEnd = 0;

        while (matcher.find()) {
            if (index >= arguments.length) {
                throw new IllegalArgumentException("Not enough arguments for placeholders in the message");
            }
            // 将当前光标到找到的 {} 之间的字符串添加到 sb 中
            sb.append(message, lastEnd, matcher.start());
            // 将 arguments 中对应的值添加到 sb 中
            sb.append(arguments[index]);
            // 更新光标位置
            lastEnd = matcher.end();
            // 更新 arguments 索引
            index++;
        }

        // 将剩余的 message 部分添加到 sb 中
        sb.append(message.substring(lastEnd));

        String result = sb.toString();
        // 检查是否有未匹配的 {}
        if (result.contains("{") || result.contains("}")) {
            throw new IllegalArgumentException("Unmatched placeholder in the message");
        }

        // 返回转换后的字符串
        return result;
    }

    /**
     * 清理字符串，清理出某些不可见字符和一些 sql 特殊字符
     *
     * @param txt 文本
     *
     * @return {String}
     */
    public static String cleanText(String txt) {
        if (txt == null) {
            return null;
        }
        return Pattern.compile("[`'\"|/,;()-+*%#·•�　\\s]").matcher(txt).replaceAll(Symbol.EMPTY);
    }

    /**
     * 获取标识符，用于参数清理
     *
     * @param param 参数
     *
     * @return 清理后的标识符
     */
    public static String cleanIdentifier(String param) {
        if (param == null) {
            return null;
        }
        StringBuilder paramBuilder = new StringBuilder();
        for (int i = 0; i < param.length(); i++) {
            char c = param.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                paramBuilder.append(c);
            }
        }
        return paramBuilder.toString();
    }

    /**
     * 判断一个字符串是否是数字
     *
     * @param str the String to check, may be null
     *
     * @return {boolean}
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将集合拼接成字符串，默认使用`,`拼接
     *
     * @param coll the {@code Collection} to convert
     *
     * @return the delimited {@code String}
     */
    public static String join(Collection<?> coll) {
        return Joiner.on(Symbol.C_COMMA).join(coll);
    }

    /**
     * 将集合拼接成字符串，默认指定分隔符
     *
     * @param coll  the {@code Collection} to convert
     * @param delim the delimiter to use (typically a ",")
     *
     * @return the delimited {@code String}
     */
    public static String join(Collection<?> coll, String delim) {
        return Joiner.on(delim).join(coll);
    }

    /**
     * 将数组拼接成字符串，默认使用`,`拼接
     *
     * @param arr the array to display
     *
     * @return the delimited {@code String}
     */
    public static String join(Object[] arr) {
        return Joiner.on(Symbol.C_COMMA).join(arr);
    }

    /**
     * 将数组拼接成字符串，默认指定分隔符
     *
     * @param arr   the array to display
     * @param delim the delimiter to use (typically a ",")
     *
     * @return the delimited {@code String}
     */
    public static String join(Object[] arr, String delim) {
        return Joiner.on(delim).join(arr);
    }

    /**
     * 将数组拼接成字符串，默认指定分隔符
     *
     * @param arr   the array to display
     * @param delim the delimiter to use (typically a ",")
     *
     * @return the delimited {@code String}
     */
    public static String join(String delim, Object... arr) {
        return Joiner.on(delim).join(arr);
    }

    /**
     * 分割 字符串
     *
     * @param str       字符串
     * @param delimiter 分割符
     *
     * @return 字符串数组
     */
    public static String[] split(String str, String delimiter) {
        return Splitter.on(delimiter).splitToStream(str).toArray(String[]::new);
    }

    /**
     * 分割 字符串 删除常见 空白符
     *
     * @param str       字符串
     * @param delimiter 分割符
     *
     * @return 字符串数组
     */
    public static String[] splitTrim(String str, String delimiter) {
        return Splitter.on(delimiter).trimResults().splitToStream(str).toArray(String[]::new);
    }

    /**
     * 字符串是否符合指定的 表达式
     *
     * <p>
     * pattern styles: "xxx*", "*xxx", "*xxx*" and "xxx*yyy"
     * </p>
     *
     * @param pattern 表达式
     * @param str     字符串
     *
     * @return 是否匹配
     */
    public static boolean simpleMatch(String pattern, String str) {
        // 检查参数是否为空
        if (pattern != null && str != null) {
            // 查找表达式中第一个通配符'*'的位置
            int firstIndex = pattern.indexOf('*');
            // 如果没有通配符，直接比较字符串和表达式是否相等
            if (firstIndex == -1) {
                return pattern.equals(str);
                // 如果通配符在表达式开头
            } else if (firstIndex == 0) {
                // 如果通配符是表达式的唯一字符，返回 true
                if (pattern.length() == 1) {
                    return true;
                } else {
                    // 查找下一个通配符的位置
                    int nextIndex = pattern.indexOf('*', 1);
                    // 如果没有更多的通配符，检查字符串是否以表达式的一部分结尾
                    if (nextIndex == -1) {
                        return str.endsWith(pattern.substring(1));
                    } else {
                        // 提取两个通配符之间的部分
                        String part = pattern.substring(1, nextIndex);
                        // 如果这部分为空，递归匹配剩余的表达式和字符串
                        if (part.isEmpty()) {
                            return simpleMatch(pattern.substring(nextIndex), str);
                        } else {
                            // 遍历字符串中所有出现的 part，尝试递归匹配
                            for (int partIndex = str.indexOf(part); partIndex != -1; partIndex = str.indexOf(part, partIndex + 1)) {
                                if (simpleMatch(pattern.substring(nextIndex), str.substring(partIndex + part.length()))) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                }
                // 如果通配符不在表达式开头
            } else {
                // 检查字符串是否以表达式的非通配符部分开头，如果是，递归匹配剩余部分
                return str.length() >= firstIndex && pattern.startsWith(str.substring(0, firstIndex)) && simpleMatch(pattern.substring(firstIndex), str.substring(firstIndex));
            }
        } else {
            // 如果参数为空，返回 false
            return false;
        }
    }

    /**
     * 字符串是否符合指定的 表达式
     *
     * <p>
     * pattern styles: "xxx*", "*xxx", "*xxx*" and "xxx*yyy"
     * </p>
     *
     * @param patterns 表达式 数组
     * @param str      字符串
     *
     * @return 是否匹配
     */
    public static boolean simpleMatch(String[] patterns, String str) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (simpleMatch(pattern, str)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 转义 HTML 用于安全过滤
     *
     * @param html html
     *
     * @return {String}
     */
    public static String escapeHtml(String html) {
        return HtmlEscapers.htmlEscaper().escape(html);
    }

    /**
     * 计算匹配项
     *
     * <p>计算子字符串在较大字符串中出现的次数。</p>
     *
     * <p>A {@code null} or empty ("") String input returns {@code 0}.</p>
     *
     * <pre>
     * StringUtils.countMatches(null, *)       = 0
     * StringUtils.countMatches("", *)         = 0
     * StringUtils.countMatches("abba", null)  = 0
     * StringUtils.countMatches("abba", "")    = 0
     * StringUtils.countMatches("abba", "a")   = 2
     * StringUtils.countMatches("abba", "ab")  = 1
     * StringUtils.countMatches("abba", "xxx") = 0
     * </pre>
     *
     * @param str the CharSequence to check, may be null
     * @param sub the substring to count, may be null
     *
     * @return the number of occurrences, 0 if either CharSequence is {@code null}
     */
    public static int countMatches(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    /**
     * 有任意一个非空
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public boolean isAnyNotBlank(Collection<String> strs) {
        if (null == strs || Array.getLength(strs) == 0) {
            return false;
        }
        return strs.stream().anyMatch(StringUtil::isNotBlank);
    }

}