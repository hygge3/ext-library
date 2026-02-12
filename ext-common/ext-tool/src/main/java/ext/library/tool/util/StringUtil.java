package ext.library.tool.util;

import org.jspecify.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串工具类，提供各种字符串处理方法
 *
 * <p>设计目的：封装常用的字符串操作，简化字符串处理逻辑，提高开发效率</p>
 *
 * <p>核心功能：
 * <ul>
 *   <li>字符串判空、转换、拼接等基本操作</li>
 *   <li>字符串格式化、模板替换功能</li>
 *   <li>字符串匹配、分割、清理等高级操作</li>
 *   <li>HTML 转义、安全过滤等安全相关操作</li>
 * </ul>
 * </p>
 *
 * <p>使用场景：
 * <ul>
 *   <li>数据验证：验证字符串是否为空、是否符合特定格式</li>
 *   <li>数据转换：将其他类型数据转换为字符串，或进行字符串格式转换</li>
 *   <li>模板处理：处理字符串模板，进行变量替换</li>
 *   <li>安全处理：对用户输入进行 HTML 转义，防止 XSS 攻击</li>
 * </ul>
 * </p>
 */
public final class StringUtil {

    /** 用于清理文本的正则表达式 */
    private static final Pattern cleanTextPattern = Pattern.compile("[`'\"|/,;()-+*%#·•�　\\s]");

    /** HTML 转义映射表 */
    private static final Map<Character, String> htmlEscapeMap = Map.of(
            '&', "&amp;",
            '<', "&lt;",
            '>', "&gt;",
            '"', "&quot;",
            '\'', "&#39;"
    );

    private StringUtil() {
    }

    /**
     * 首字母变小写
     *
     * @param str 字符串
     *
     * @return 首字母小写后的字符串
     */
    public static String firstCharToLower(String str) {
        if (str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (firstChar >= 'A' && firstChar <= 'Z') {
            char[] chars = str.toCharArray();
            chars[0] += ('a' - 'A');
            return new String(chars);
        }
        return str;
    }

    /**
     * 首字母变大写
     *
     * @param str 字符串
     *
     * @return 首字母大写后的字符串
     */
    public static String firstCharToUpper(String str) {
        if (str.isEmpty()) {
            return str;
        }
        char firstChar = str.charAt(0);
        if (firstChar >= 'a' && firstChar <= 'z') {
            char[] chars = str.toCharArray();
            chars[0] -= ('a' - 'A');
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
    public static boolean isEmpty(@Nullable String str) {
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
    public static boolean isNotEmpty(@Nullable String str) {
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
    public static boolean isBlank(@Nullable String str) {
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
    public static boolean isNotBlank(@Nullable String str) {
        return !isBlank(str);
    }

    /**
     * 判断是否有任意一个空字符串
     *
     * @param strs 字符串数组
     *
     * @return boolean
     */
    public static boolean isAnyBlank(@Nullable String... strs) {
        if (strs.length == 0) {
            return true;
        }
        return Stream.of(strs).anyMatch(StringUtil::isBlank);
    }

    /**
     * 判断是否有任意一个空字符串
     *
     * @param strs 字符串集合
     *
     * @return boolean
     */
    public static boolean isAnyBlank(@Nullable Collection<String> strs) {
        if (strs == null || strs.isEmpty()) {
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
     * 判断是否有任意一个非空字符串
     *
     * @param strs 字符串数组
     *
     * @return boolean
     */
    public static boolean isAnyNotBlank(String... strs) {
        if (strs.length == 0) {
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
     * 是否以指定字符开头
     *
     * @param cs CharSequence
     * @param c  字符
     *
     * @return {boolean}
     */
    public static boolean startWith(CharSequence cs, char c) {
        return !cs.isEmpty() && cs.charAt(0) == c;
    }

    /**
     * 是否以指定字符串结尾
     *
     * @param str    被检测字符串
     * @param suffix 结尾字符串
     *
     * @return {boolean}
     */
    public static boolean endWith(String str, String suffix) {
        if (isEmpty(str)) {
            return false;
        }
        return str.endsWith(suffix);
    }

    /**
     * 是否以指定字符结尾
     *
     * @param cs CharSequence
     * @param c  字符
     *
     * @return {boolean}
     */
    public static boolean endWith(CharSequence cs, char c) {
        return !cs.isEmpty() && cs.charAt(cs.length() - 1) == c;
    }

    /**
     * 将字符串中特定模式的字符转换成 map 中对应的值
     * <p>
     * 支持：
     * <p>
     * - ${key} 变量替换
     * - \${key} 转义
     * - ${key:default} 默认值
     * use: format("my name is ${name}, and i like ${like}!", {"name":"L.cm", "like":
     * "Java"})
     *
     * @param message 需要转换的字符串
     * @param params  转换所需的键值对集合
     *
     * @return 转换后的字符串
     */
    public static String format(String message, Map<String, ?> params) {
        StringBuilder sb = new StringBuilder();
        int i = 0;

        while (i < message.length()) {
            // 转义处理
            if (message.startsWith("\\${", i)) {
                sb.append("${");
                i += 3;
                continue;
            }

            // 变量替换
            if (message.startsWith("${", i)) {
                int end = message.indexOf('}', i);
                if (end != -1) {
                    String expr = message.substring(i + 2, end);
                    String[] parts = expr.split(":", 2);
                    String key = parts[0];
                    String defaultValue = parts.length > 1 ? parts[1] : "";

                    Object value = params.get(key);
                    sb.append(value != null ? value :
                            (defaultValue.isEmpty() ? "${" + expr + "}" : defaultValue));
                    i = end + 1;
                    continue;
                }
            }

            sb.append(message.charAt(i));
            i++;
        }
        return sb.toString();
    }

    /**
     * 同 log 格式的 format 规则
     * <p>
     * use: format("my name is {}, and i like {}!", "L.cm", "Java")
     * <p>
     * 用 SLF4J/Log4j 的 MessageFormatter 实现
     *
     * @param message   需要转换的字符串
     * @param arguments 需要替换的变量
     *
     * @return 转换后的字符串
     */
    public static String format(String message, Object... arguments) {
        return MessageFormatter.arrayFormat(message, arguments).getMessage();
    }

    /**
     * 清理字符串，清理出某些不可见字符和一些 sql 特殊字符
     *
     * @param txt 文本
     *
     * @return {String}
     */
    public static String cleanText(String txt) {
        return cleanTextPattern.matcher(txt).replaceAll("");
    }

    /**
     * 获取标识符，用于参数清理
     *
     * @param param 参数
     *
     * @return 清理后的标识符
     */
    public static String cleanIdentifier(String param) {
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
        return join(coll, ",");
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
        return coll.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(delim));
    }

    /**
     * 将数组拼接成字符串，默认使用`,`拼接
     *
     * @param arr the array to display
     *
     * @return the delimited {@code String}
     */
    public static String join(Object[] arr) {
        return join(arr, ",");
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
        return Arrays.stream(arr)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(delim));
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
        return join(arr, delim);
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
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty()).toArray(String[]::new);
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
        for (String pattern : patterns) {
            if (simpleMatch(pattern, str)) {
                return true;
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
        if (isBlank(html)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(html.length());
        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            String replacement = htmlEscapeMap.get(c);
            if (replacement != null) {
                sb.append(replacement);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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
     * 检查集合中是否有任意一个非空字符串
     *
     * <pre>
     * StringUtil.isAnyNotBlank(Collections.emptyList()) = false
     * StringUtil.isAnyNotBlank(Arrays.asList(null, "", " ")) = false
     * StringUtil.isAnyNotBlank(Arrays.asList("", "test", " ")) = true
     * </pre>
     *
     * @param strs 字符串集合，可能为 null
     *
     * @return true 如果集合中存在至少一个非空字符串，否则 false
     */
    public static boolean isAnyNotBlank(@Nullable Collection<String> strs) {
        if (strs == null || strs.isEmpty()) {
            return false;
        }
        return strs.stream().anyMatch(StringUtil::isNotBlank);
    }

}
