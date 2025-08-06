package ext.library.tool;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import com.google.common.html.HtmlEscapers;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import ext.library.tool.constant.Holder;
import ext.library.tool.constant.Symbol;
import ext.library.tool.core.Exceptions;
import ext.library.tool.domain.ObjectId;
import ext.library.tool.util.BasicConverter;
import ext.library.tool.util.BoolUtil;
import ext.library.tool.util.DateUtil;
import lombok.experimental.UtilityClass;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 工具包集合，工具类快捷方式
 */
@UtilityClass
public class $ {

    // region Assert

    /**
     * 断言，必须不能为 null <blockquote><pre>
     * public Foo(Bar bar) {
     *     this.bar = $.requireNotNull(bar);
     * }
     * </pre></blockquote>
     *
     * @param obj the object reference to check for nullity
     * @param <T> the type of the reference
     *
     * @return {@code obj} if not {@code null}
     *
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public <T> T requireNotNull(T obj) {
        return Objects.requireNonNull(obj);
    }

    /**
     * 断言，必须不能为 null <blockquote><pre>
     * public Foo(Bar bar, Baz baz) {
     *     this.bar = $.requireNotNull(bar, "bar must not be null");
     *     this.baz = $.requireNotNull(baz, "baz must not be null");
     * }
     * </pre></blockquote>
     *
     * @param obj     the object reference to check for nullity
     * @param message detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @param <T>     the type of the reference
     *
     * @return {@code obj} if not {@code null}
     *
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public <T> T requireNotNull(T obj, String message) {
        return Objects.requireNonNull(obj, message);
    }

    /**
     * 断言，必须不能为 null <blockquote><pre>
     * public Foo(Bar bar, Baz baz) {
     *     this.bar = $.requireNotNull(bar, () -> "bar must not be null");
     * }
     * </pre></blockquote>
     *
     * @param obj             the object reference to check for nullity
     * @param messageSupplier supplier of the detail message to be used in the event that
     *                        a {@code NullPointerException} is thrown
     * @param <T>             the type of the reference
     *
     * @return {@code obj} if not {@code null}
     *
     * @throws NullPointerException if {@code obj} is {@code null}
     */
    public <T> T requireNotNull(T obj, Supplier<String> messageSupplier) {
        return Objects.requireNonNull(obj, messageSupplier);
    }

    // endregion

    // region Object

    /**
     * 判断对象为 true
     *
     * @param object 对象
     *
     * @return 对象是否为 true
     */
    public boolean isTrue(Object object) {
        return BoolUtil.isTrue(object);
    }

    /**
     * 判断对象为 false
     *
     * @param object 对象
     *
     * @return 对象是否为 false
     */
    public boolean isFalse(Object object) {
        return BoolUtil.isFalse(object);
    }

    /**
     * 判断对象是否为 null
     * <p>
     * This method exists to be used as a {@link java.util.function.Predicate},
     * {@code context($::isNull)}
     * </p>
     *
     * @param obj a reference to be checked against {@code null}
     *
     * @return {@code true} if the provided reference is {@code null} otherwise
     * {@code false}
     *
     * @see java.util.function.Predicate
     */
    public boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    /**
     * 判断对象是否 not null
     * <p>
     * This method exists to be used as a {@link java.util.function.Predicate},
     * {@code context($::notNull)}
     * </p>
     *
     * @param obj a reference to be checked against {@code null}
     *
     * @return {@code true} if the provided reference is non-{@code null} otherwise
     * {@code false}
     *
     * @see java.util.function.Predicate
     */
    public boolean isNotNull(Object obj) {
        return Objects.nonNull(obj);
    }

    /**
     * 获取对象的元素数量或长度
     * 此方法旨在提供一个通用的途径来获取不同类型的对象的大小信息，包括集合、数组、迭代器等
     * 对于非集合、非数组、非迭代器类型的对象，假设大小为 1，反映其存在性
     *
     * @param obj 要检查其大小的对象
     *
     * @return 对象的元素数量或长度如果对象为 null，则返回 0
     */
    public int size(Object obj) {
        // 检查对象是否为 null，null 对象返回大小为 0
        if (null == obj) {
            return 0;
        }
        // 如果对象是 Collection 类型，直接调用其 size 方法返回大小
        else if (obj instanceof Collection<?> coll) {
            return coll.size();
        }
        // 如果对象是 Map 类型，直接调用其 size 方法返回大小
        else if (obj instanceof Map<?, ?> map) {
            return map.size();
        }
        // 如果对象是 Iterable 类型，将其转换为 List 后返回大小
        else if (obj instanceof Iterable<?> iter) {
            return Lists.newArrayList(iter).size();
        }
        // 如果对象是 Iterator 类型，使用 Iterators 工具类的 size 方法返回大小
        else if (obj instanceof Iterator<?> iter) {
            return Iterators.size(iter);
        }
        // 如果对象是数组类型，使用 Array 类的 getLength 方法返回数组长度
        else if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        // 对于非上述类型的对象，普通对象大小为 1
        return 1;
    }

    /**
     * 判断对象是数组
     *
     * @param obj the object to check
     *
     * @return 是否数组
     */
    public boolean isArray(Object obj) {
        return obj.getClass().isArray();
    }

    /**
     * 判断空对象 object、map、list、set、字符串、数组
     *
     * @param obj the object to check
     *
     * @return 数组是否为空
     */
    public boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj instanceof Optional<?> optional) {
            return optional.isEmpty();
        } else if (obj instanceof CharSequence cs) {
            return cs.isEmpty();
        } else if (obj instanceof Collection<?> coll) {
            return coll.isEmpty();
        } else if (obj instanceof Map<?, ?> map) {
            return map.isEmpty();
        } else if (obj instanceof Iterable<?> iter) {
            return !iter.iterator().hasNext();
        } else if (obj instanceof Iterator<?> iter) {
            return !iter.hasNext();
        } else if (isArray(obj)) {
            return Array.getLength(obj) == 0;
        }
        return false;
    }

    /**
     * 对象不为空 object、map、list、set、字符串、数组
     *
     * @param obj the object to check
     *
     * @return 是否不为空
     */
    public boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 安全的 equals
     *
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     *
     * @return whether the given objects are equal
     *
     * @see Object#equals(Object)
     * @see Arrays#equals
     */
    public boolean equalsSafe(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null && o2 != null) {
            return o1.equals(o2);
        } else {
            return false;
        }
    }

    /**
     * 对象 eq
     *
     * @param o1 Object
     * @param o2 Object
     *
     * @return 是否 eq
     */
    public boolean equals(Object o1, Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * 比较两个对象是否不相等。<br>
     *
     * @param o1 对象 1
     * @param o2 对象 2
     *
     * @return 是否不 eq
     */
    public boolean isNotEqual(Object o1, Object o2) {
        return !equals(o1, o2);
    }

    /**
     * 返回对象的 hashCode
     *
     * @param obj Object
     *
     * @return hashCode
     */
    public int hashCode(Object obj) {
        return Objects.hashCode(obj);
    }

    /**
     * 如果对象为 null，返回默认值
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return Object
     */
    public <T> T defaultIfNull(T object, T defaultValue) {
        return isNull(object) ? defaultValue : object;
    }

    /**
     * 如果对象为空，返回默认值
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return Object
     */
    public <T> T defaultIfEmpty(T object, T defaultValue) {
        return isEmpty(object) ? defaultValue : object;
    }

    // endregion

    // region Array

    /**
     * 判断数组为空
     *
     * @param array the array to check
     *
     * @return 数组是否为空
     */
    public boolean isEmpty(Object[] array) {
        return null == array || Array.getLength(array) == 0;
    }

    /**
     * 判断数组不为空
     *
     * @param array 数组
     *
     * @return 数组是否不为空
     */
    public boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 判断数组中是否包含元素
     *
     * @param array   the Array to check
     * @param element the element to look for
     * @param <T>     The generic tag
     *
     * @return {@code true} if found, {@code false} else
     */
    public <T> boolean contains(T[] array, final T element) {
        if (array == null) {
            return false;
        }
        return Arrays.stream(array).anyMatch(x -> equalsSafe(x, element));
    }

    // endregion

    // region String

    /**
     * 首字母变小写
     *
     * @param str 字符串
     *
     * @return {String}
     */
    public String firstCharToLower(String str) {
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
    public String firstCharToUpper(String str) {
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
    public boolean isBlank(String str) {
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
    public boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断是否有任意一个 空字符串
     *
     * @param strs String
     *
     * @return boolean
     */
    public boolean isAnyBlank(String... strs) {
        if (null == strs || Array.getLength(strs) == 0) {
            return true;
        }
        return Stream.of(strs).anyMatch($::isBlank);
    }

    /**
     * 有 任意 一个 Blank
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public boolean isAnyBlank(Collection<String> strs) {
        if (null == strs || strs.isEmpty()) {
            return true;
        }
        return strs.stream().anyMatch($::isBlank);
    }

    /**
     * 判断是否全为非空字符串
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public boolean isNoneBlank(String... strs) {
        return !isAnyBlank(strs);
    }

    /**
     * 判断是否全为非空字符串
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public boolean isNoneBlank(Collection<String> strs) {
        return !isAnyBlank(strs);
    }

    /**
     * 有任意一个非空
     *
     * @param strs 字符串列表
     *
     * @return boolean
     */
    public boolean isAnyNotBlank(String... strs) {
        if (null == strs || Array.getLength(strs) == 0) {
            return false;
        }
        return Stream.of(strs).anyMatch($::isNotBlank);
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
        return strs.stream().anyMatch($::isNotBlank);
    }

    /**
     * 是否以指定字符串开头
     *
     * @param str    被检测字符串
     * @param prefix 开头字符串
     *
     * @return {boolean}
     */
    public boolean startWith(String str, String prefix) {
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
    public boolean startWith(CharSequence cs, char c) {
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
    public boolean endWith(String str, String prefix) {
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
    public boolean endWith(CharSequence cs, char c) {
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
    public String format(String message, Map<String, ?> params) {
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
    public String format(String message, Object... arguments) {
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
    public String cleanText(String txt) {
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
    public String cleanIdentifier(String param) {
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
    public boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if (chr < 48 || chr > 57) {
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
    public String join(Collection<?> coll) {
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
    public String join(Collection<?> coll, String delim) {
        return Joiner.on(delim).join(coll);
    }

    /**
     * 将数组拼接成字符串，默认使用`,`拼接
     *
     * @param arr the array to display
     *
     * @return the delimited {@code String}
     */
    public String join(Object[] arr) {
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
    public String join(Object[] arr, String delim) {
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
    public String[] split(String str, String delimiter) {
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
    public String[] splitTrim(String str, String delimiter) {
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
    public boolean simpleMatch(String pattern, String str) {
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
    public boolean simpleMatch(String[] patterns, String str) {
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
    public String escapeHtml(String html) {
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

    // endregion

    // region ID

    /**
     * 生成 uuid
     * 长度：32
     *
     * @return {@code String }
     */
    public String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * UUID 版本 7 (v7) 由时间戳和随机数据生成。
     * 长度：32
     *
     * @return {@code String }
     */
    public static String getUUIDv7() {
        // random bytes
        byte[] value = new byte[16];
        Holder.SECURE_RANDOM.nextBytes(value);

        // current timestamp in ms
        long timestamp = System.currentTimeMillis();

        // timestamp
        value[0] = (byte) ((timestamp >> 40) & 0xFF);
        value[1] = (byte) ((timestamp >> 32) & 0xFF);
        value[2] = (byte) ((timestamp >> 24) & 0xFF);
        value[3] = (byte) ((timestamp >> 16) & 0xFF);
        value[4] = (byte) ((timestamp >> 8) & 0xFF);
        value[5] = (byte) (timestamp & 0xFF);

        // version and variant
        value[6] = (byte) ((value[6] & 0x0F) | 0x70);
        value[8] = (byte) ((value[8] & 0x3F) | 0x80);
        ByteBuffer buf = ByteBuffer.wrap(value);
        long high = buf.getLong();
        long low = buf.getLong();
        return new UUID(high, low).toString().replaceAll("-", "");
    }

    /**
     * 生成 ULID
     * 长度：26
     *
     * @return {@code String }
     */
    public String getULID() {
        return Holder.ULID.nextULID();
    }

    /**
     * 生成 ObjectId
     * 长度：24
     *
     * @return {@code String }
     */
    public String getObjectId() {
        return ObjectId.get().toHexString();
    }

    /**
     * 生成 SnowflakeId
     * 长度：16
     *
     * @return {@code String }
     */
    public String getSnowflakeId() {
        return String.valueOf(Holder.SNOWFLAKE_ID.nextId());
    }

    /**
     * Sqids 编码
     *
     * @param numbers 数字
     *
     * @return {@code String }
     */
    public String sqidsEncode(List<Long> numbers) {
        return Holder.SQIDS.encode(numbers);
    }

    /**
     * Sqids 解码
     *
     * @param sqids SQIDS
     *
     * @return {@code List<Long> }
     */
    public List<Long> sqidsDecode(String sqids) {
        return Holder.SQIDS.decode(sqids);
    }

    /**
     * 随机数生成
     *
     * @param count 字符长度
     *
     * @return 随机数
     */
    public String random(int count) {
        if (count == 0) {
            return "";
        }
        Preconditions.checkArgument(count > 0, "Requested random string length %s is less than 0.", count);
        final byte[] buffer = new byte[5];
        Holder.RANDOM.nextBytes(buffer);
        return BaseEncoding.base64Url().omitPadding().encode(buffer); // or base32()
    }

    // endregion

    // region Collection

    /**
     * 判断迭代器中是否包含元素
     *
     * @param iterator the Iterator to check
     * @param element  the element to look for
     *
     * @return {@code true} if found, {@code false} otherwise
     */
    public boolean contains(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (equalsSafe(candidate, element)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断枚举是否包含该元素
     *
     * @param enumeration the Enumeration to check
     * @param element     the element to look for
     *
     * @return {@code true} if found, {@code false} otherwise
     */
    public boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (equalsSafe(candidate, element)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Concatenates 2 arrays
     *
     * @param one   数组 1
     * @param other 数组 2
     *
     * @return 新数组
     */
    public String[] concat(String[] one, String[] other) {
        return concat(one, other, String.class);
    }

    /**
     * Concatenates 2 arrays
     *
     * @param one   数组 1
     * @param other 数组 2
     * @param clazz 数组类
     *
     * @return 新数组
     */
    @SuppressWarnings("unchecked")
    public <T> T[] concat(T[] one, T[] other, Class<T> clazz) {
        T[] target = (T[]) Array.newInstance(clazz, one.length + other.length);
        System.arraycopy(one, 0, target, 0, one.length);
        System.arraycopy(other, 0, target, one.length, other.length);
        return target;
    }

    /**
     * 不可变 Set
     *
     * @param es  对象
     * @param <E> 泛型
     *
     * @return 集合
     */
    @SafeVarargs
    public <E> Set<E> ofImmutableSet(E... es) {
        return ImmutableSet.copyOf(es);
    }

    /**
     * 不可变 List
     *
     * @param es  对象
     * @param <E> 泛型
     *
     * @return 集合
     */
    @SafeVarargs
    public <E> List<E> ofImmutableList(E... es) {
        return ImmutableList.copyOf(es);
    }

    /**
     * Iterable 转换为 List 集合
     *
     * @param elements Iterable
     * @param <E>      泛型
     *
     * @return 集合
     */
    public <E> List<E> toList(Iterable<E> elements) {
        Objects.requireNonNull(elements, "elements es is null.");
        if (elements instanceof Collection) {
            return new ArrayList<>((Collection<E>) elements);
        }
        Iterator<E> iterator = elements.iterator();
        List<E> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * 将 key value 数组转为 map
     *
     * @param keysValues key value 数组
     * @param <K>        key
     * @param <V>        value
     *
     * @return map 集合
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> toMap(Object... keysValues) {
        int kvLength = keysValues.length;
        if (kvLength % 2 != 0) {
            throw new IllegalArgumentException("wrong number of arguments for met, keysValues length can not be odd");
        }
        Map<K, V> keyValueMap = new HashMap<>(kvLength);
        for (int i = kvLength - 2; i >= 0; i -= 2) {
            Object key = keysValues[i];
            Object value = keysValues[i + 1];
            keyValueMap.put((K) key, (V) value);
        }
        return keyValueMap;
    }

    /**
     * list 分片
     *
     * @param list List
     * @param size 分片大小
     * @param <T>  泛型
     *
     * @return List 分片
     */
    public <T> List<List<T>> partition(List<T> list, int size) {
        Objects.requireNonNull(list, "List to partition must not null.");
        Preconditions.checkArgument(size > 0, "List to partition size must more then zero.");
        return Lists.partition(list, size);
    }

    // endregion

    // region Converter

    /**
     * 强转 string
     *
     * @param object Object
     *
     * @return String
     */
    public String toStr(Object object) {
        return toStr(object, null);
    }

    /**
     * 强转 string
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return String
     */
    public String toStr(Object object, String defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        if (object instanceof CharSequence cs) {
            return cs.toString();
        }
        return String.valueOf(object);
    }

    /**
     * 对象转为 int（支持 String 和 Number），默认：0
     *
     * @param object Object
     *
     * @return int
     */
    public int toInt(Object object) {
        return toInt(object, 0);
    }

    /**
     * 对象转为 int（支持 String 和 Number）
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return int
     */
    public int toInt(Object object, int defaultValue) {
        if (object instanceof Number number) {
            return number.intValue();
        }
        if (object instanceof CharSequence cs) {
            String value = cs.toString();
            try {
                return Integer.parseInt(value);
            } catch (final NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 对象转为 long（支持 String 和 Number），默认：0L
     *
     * @param object Object
     *
     * @return long
     */
    public long toLong(Object object) {
        return toLong(object, 0L);
    }

    /**
     * 对象转为 long（支持 String 和 Number），默认：0L
     *
     * @param object Object
     *
     * @return long
     */
    public long toLong(Object object, long defaultValue) {
        if (object instanceof Number number) {
            return number.longValue();
        }
        if (object instanceof CharSequence cs) {
            String value = cs.toString();
            try {
                return Long.parseLong(value);
            } catch (final NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 对象转为 Float
     *
     * @param object Object
     *
     * @return 结果
     */
    public float toFloat(Object object) {
        return toFloat(object, 0.0f);
    }

    /**
     * 对象转为 Float
     *
     * @param object       Object
     * @param defaultValue float
     *
     * @return 结果
     */
    public float toFloat(Object object, float defaultValue) {
        if (object instanceof Number number) {
            return number.floatValue();
        }
        if (object instanceof CharSequence cs) {
            String value = cs.toString();
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 对象转为 Double
     *
     * @param object Object
     *
     * @return 结果
     */
    public double toDouble(Object object) {
        return toDouble(object, 0.0d);
    }

    /**
     * 对象转为 Double
     *
     * @param object       Object
     * @param defaultValue double
     *
     * @return 结果
     */
    public double toDouble(Object object, double defaultValue) {
        if (object instanceof Number number) {
            return number.doubleValue();
        }
        if (object instanceof CharSequence cs) {
            String value = cs.toString();
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 对象转为 Byte
     *
     * @param object Object
     *
     * @return 结果
     */
    public byte toByte(Object object) {
        return toByte(object, (byte) 0);
    }

    /**
     * 对象转为 Byte
     *
     * @param object       Object
     * @param defaultValue byte
     *
     * @return 结果
     */
    public byte toByte(Object object, byte defaultValue) {
        if (object instanceof Number number) {
            return number.byteValue();
        }
        if (object instanceof CharSequence cs) {
            String value = cs.toString();
            try {
                return Byte.parseByte(value);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 对象转为 Short
     *
     * @param object Object
     *
     * @return 结果
     */
    public short toShort(Object object) {
        return toShort(object, (short) 0);
    }

    /**
     * 对象转为 Short
     *
     * @param object       Object
     * @param defaultValue short
     *
     * @return 结果
     */
    public short toShort(Object object, short defaultValue) {
        if (object instanceof Number number) {
            return number.byteValue();
        }
        if (object instanceof CharSequence cs) {
            String value = cs.toString();
            try {
                return Short.parseShort(value);
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * 对象转为 BigDecimal
     *
     * @param object Object
     *
     * @return 结果
     */

    public BigDecimal toBigDecimal(Object object) {
        return toBigDecimal(object, null);
    }

    /**
     * 对象转为 BigDecimal
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return 结果
     */
    public BigDecimal toBigDecimal(Object object, Boolean defaultValue) {
        return switch (object) {
            case null -> null;
            case BigDecimal bigDecimal -> bigDecimal;
            case String string -> new BigDecimal(string);
            case BigInteger bigInteger -> new BigDecimal(bigInteger);
            case Number number -> BigDecimal.valueOf(number.doubleValue());
            default -> new BigDecimal(String.valueOf(object));
        };
    }

    /**
     * 对象转为 Boolean
     *
     * @param object Object
     *
     * @return 结果
     */

    public Boolean toBoolean(Object object) {
        return toBoolean(object, null);
    }

    /**
     * 对象转为 Boolean
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return 结果
     */

    public Boolean toBoolean(Object object, Boolean defaultValue) {
        if (object instanceof Boolean bool) {
            return bool;
        } else if (object instanceof CharSequence cs) {
            String value = cs.toString();
            if ("true".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value) || "1".equalsIgnoreCase(value)) {
                return true;
            } else if ("false".equalsIgnoreCase(value) || "n".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value) || "0".equalsIgnoreCase(value)) {
                return false;
            }
        }
        return defaultValue;
    }

    /**
     * 将 long 转短字符串 为 62 进制
     *
     * @param num 数字
     *
     * @return 短字符串
     */
    public String to62Str(long num) {
        byte[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '-'};
        int radix = 62;
        byte[] buf = new byte[65];
        int charPos = 64;
        num = -num;
        while (num <= -radix) {
            buf[charPos--] = DIGITS[(int) (-(num % radix))];
            num = num / radix;
        }
        buf[charPos] = DIGITS[(int) (-num)];
        return new String(buf, charPos, (65 - charPos), StandardCharsets.UTF_8);
    }

    /**
     * 数据类型转换
     *
     * @param source     the source object
     * @param targetType the target type
     * @param <T>        泛型标记
     *
     * @return the converted value
     *
     * @throws IllegalArgumentException if targetType is {@code null}, or sourceType is
     *                                  {@code null} but source is not {@code null}
     */
    public <T> T convert(Object source, Class<T> targetType) {
        if (source.getClass().isAssignableFrom(targetType)) {
            return (T) source;
        }
        return BasicConverter.cast(source, targetType);
    }

    // endregion

    // region Encode & Decode

    /**
     * Calculates the MD5 digest.
     *
     * @param data Data to digest
     *
     * @return MD5 digest as a hex array
     */
    public byte[] md5(final byte[] data) {
        return Hashing.md5().hashBytes(data).asBytes();
    }

    /**
     * Calculates the MD5 digest.
     *
     * @param data Data to digest
     *
     * @return MD5 digest as a hex array
     */
    public byte[] md5(final String data) {
        return Hashing.md5().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * Calculates the MD5 digest and returns the value as a 32 character hex string.
     *
     * @param data Data to digest
     *
     * @return MD5 digest as a hex string
     */
    public String md5Hex(final String data) {
        return Hashing.md5().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * Return a hexadecimal string representation of the MD5 digest of the given bytes.
     *
     * @param bytes the bytes to calculate the digest over
     *
     * @return a hexadecimal digest string
     */
    public String md5Hex(final byte[] bytes) {
        return Hashing.md5().hashBytes(bytes).toString();
    }

    /**
     * sha1
     *
     * @param data Data to digest
     *
     * @return digest as a hex array
     */
    public byte[] sha1(String data) {
        return Hashing.sha1().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha1
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex array
     */
    public byte[] sha1(final byte[] bytes) {
        return Hashing.sha1().hashBytes(bytes).asBytes();
    }

    /**
     * sha1Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public String sha1Hex(String data) {
        return Hashing.sha1().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha1Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public String sha1Hex(final byte[] bytes) {
        return Hashing.sha1().hashBytes(bytes).toString();
    }

    /**
     * sha256Hex
     *
     * @param data Data to digest
     *
     * @return digest as a byte array
     */
    public byte[] sha256(String data) {
        return Hashing.sha256().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha256Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a byte array
     */
    public byte[] sha256(final byte[] bytes) {
        return Hashing.sha256().hashBytes(bytes).asBytes();
    }

    /**
     * sha256Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public String sha256Hex(String data) {
        return Hashing.sha256().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha256Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public String sha256Hex(final byte[] bytes) {
        return Hashing.sha256().hashBytes(bytes).toString();
    }

    /**
     * sha384
     *
     * @param data Data to digest
     *
     * @return digest as a byte array
     */
    public byte[] sha384(String data) {
        return Hashing.sha384().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha384
     *
     * @param bytes Data to digest
     *
     * @return digest as a byte array
     */
    public byte[] sha384(final byte[] bytes) {
        return Hashing.sha384().hashBytes(bytes).asBytes();
    }

    /**
     * sha384Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public String sha384Hex(String data) {
        return Hashing.sha384().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha384Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public String sha384Hex(final byte[] bytes) {
        return Hashing.sha384().hashBytes(bytes).toString();
    }

    /**
     * sha512Hex
     *
     * @param data Data to digest
     *
     * @return digest as a byte array
     */
    public byte[] sha512(String data) {
        return Hashing.sha512().hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * sha512Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a byte array
     */
    public byte[] sha512(final byte[] bytes) {
        return Hashing.sha512().hashBytes(bytes).asBytes();
    }

    /**
     * sha512Hex
     *
     * @param data Data to digest
     *
     * @return digest as a hex string
     */
    public String sha512Hex(String data) {
        return Hashing.sha512().hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * sha512Hex
     *
     * @param bytes Data to digest
     *
     * @return digest as a hex string
     */
    public String sha512Hex(final byte[] bytes) {
        return Hashing.sha512().hashBytes(bytes).toString();
    }

    /**
     * hmacMd5
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public byte[] hmacMd5(String data, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacMd5
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public byte[] hmacMd5(final byte[] bytes, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacMd5 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public String hmacMd5Hex(String data, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacMd5 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public String hmacMd5Hex(final byte[] bytes, String key) {
        return Hashing.hmacMd5(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * hmacSha1
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public byte[] hmacSha1(String data, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacSha1
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public byte[] hmacSha1(final byte[] bytes, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacSha1 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public String hmacSha1Hex(String data, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacSha1 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public String hmacSha1Hex(final byte[] bytes, String key) {
        return Hashing.hmacSha1(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * hmacSha256
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public byte[] hmacSha256(String data, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacSha256
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public byte[] hmacSha256(final byte[] bytes, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacSha256 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public String hmacSha256Hex(String data, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacSha256 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public String hmacSha256Hex(final byte[] bytes, String key) {
        return Hashing.hmacSha256(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * hmacSha512
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a byte array
     */
    public byte[] hmacSha512(String data, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).asBytes();
    }

    /**
     * hmacSha512
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a byte array
     */
    public byte[] hmacSha512(final byte[] bytes, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).asBytes();
    }

    /**
     * hmacSha512 Hex
     *
     * @param data Data to digest
     * @param key  key
     *
     * @return digest as a hex string
     */
    public String hmacSha512Hex(String data, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashString(data, StandardCharsets.UTF_8).toString();
    }

    /**
     * hmacSha512 Hex
     *
     * @param bytes Data to digest
     * @param key   key
     *
     * @return digest as a hex string
     */
    public String hmacSha512Hex(final byte[] bytes, String key) {
        return Hashing.hmacSha512(key.getBytes(StandardCharsets.UTF_8)).hashBytes(bytes).toString();
    }

    /**
     * byte 数组序列化成 hex
     *
     * @param bytes bytes to encode
     *
     * @return MD5 digest as a hex string
     */
    public String encodeHex(byte[] bytes) {
        HexFormat hexFormat = HexFormat.of();
        return hexFormat.formatHex(bytes);
    }

    /**
     * 字符串反序列化成 hex
     *
     * @param hexString String to decode
     *
     * @return MD5 digest as a hex string
     */
    public byte[] decodeHex(final String hexString) {
        HexFormat hexFormat = HexFormat.of();
        return hexFormat.parseHex(hexString);
    }

    /**
     * Base64 编码
     *
     * @param value 字符串
     *
     * @return {String}
     */
    public String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 编码
     *
     * @param value   字符串
     * @param charset 字符集
     *
     * @return {String}
     */
    public String encodeBase64(String value, Charset charset) {
        return Base64.getEncoder().encodeToString(value.getBytes(charset));
    }

    /**
     * Base64 编码为 URL 安全
     *
     * @param value 字符串
     *
     * @return {String}
     */
    public String encodeBase64UrlSafe(String value) {
        return Base64.getUrlEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64 编码为 URL 安全
     *
     * @param value   字符串
     * @param charset 字符集
     *
     * @return {String}
     */
    public String encodeBase64UrlSafe(String value, Charset charset) {
        return Base64.getUrlEncoder().encodeToString(value.getBytes(charset));
    }

    /**
     * Base64 解码
     *
     * @param value 字符串
     *
     * @return {String}
     */
    public String decodeBase64(String value) {
        return new String(Base64.getDecoder().decode(value));
    }

    /**
     * Base64 解码
     *
     * @param value   字符串
     * @param charset 字符集
     *
     * @return {String}
     */
    public String decodeBase64(String value, Charset charset) {
        return new String(Base64.getDecoder().decode(value.getBytes(charset)), charset);
    }

    /**
     * Base64URL 安全解码
     *
     * @param value 字符串
     *
     * @return {String}
     */
    public String decodeBase64UrlSafe(String value) {
        return new String(Base64.getUrlDecoder().decode(value));
    }

    /**
     * Base64URL 安全解码
     *
     * @param value   字符串
     * @param charset 字符集
     *
     * @return {String}
     */
    public String decodeBase64UrlSafe(String value, Charset charset) {
        return new String(Base64.getUrlDecoder().decode(value.getBytes(charset)), charset);
    }

    /**
     * url 编码
     *
     * @param source the String to be encoded
     *
     * @return the encoded String
     */
    public String urlEncode(String source) {
        return Base64.getUrlEncoder().encodeToString(source.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * url 编码
     *
     * @param source  the String to be encoded
     * @param charset the character encoding to encode to
     *
     * @return the encoded String
     */
    public String urlEncode(String source, Charset charset) {
        return Base64.getUrlEncoder().encodeToString(source.getBytes(charset));
    }

    /**
     * url 解码
     *
     * @param source the encoded String
     *
     * @return the decoded value
     *
     * @throws IllegalArgumentException when the given source contains invalid encoded
     *                                  sequences
     * @see java.net.URLDecoder#decode(String, String)
     */
    public String urlDecode(String source) {
        return new String(Base64.getUrlDecoder().decode(source));
    }

    /**
     * url 解码
     *
     * @param source  the encoded String
     * @param charset the character encoding to use
     *
     * @return the decoded value
     *
     * @throws IllegalArgumentException when the given source contains invalid encoded
     *                                  sequences
     * @see java.net.URLDecoder#decode(String, String)
     */
    public String urlDecode(String source, Charset charset) {
        return new String(Base64.getUrlDecoder().decode(source), charset);
    }

    // endregion

    // region IO

    /**
     * 关闭 Closeable
     *
     * @param closeable 自动关闭
     */
    public void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        if (closeable instanceof Flushable flushable) {
            try {
                flushable.flush();
            } catch (IOException ignored) {
                // ignore
            }
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
            // ignore
        }
    }

    /**
     * InputStream to String utf-8
     *
     * @param input the <code>InputStream</code> to read from
     *
     * @return the requested String
     *
     * @throws NullPointerException if the input is null
     */
    public String readToString(InputStream input) {
        try (input) {
            return new String(ByteStreams.toByteArray(input));
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * InputStream to String
     *
     * @param input   the <code>InputStream</code> to read from
     * @param charset the <code>Charset</code>
     *
     * @return the requested String
     *
     * @throws NullPointerException if the input is null
     */
    public String readToString(InputStream input, Charset charset) {
        try (input) {
            return new String(ByteStreams.toByteArray(input), charset);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * InputStream to bytes 数组
     *
     * @param input InputStream
     *
     * @return the requested byte array
     */
    public byte[] readToByteArray(InputStream input) {
        try (input) {
            return ByteStreams.toByteArray(input);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取文件为字符串
     *
     * @param file the file to read, must not be {@code null}
     *
     * @return the file contents, never {@code null}
     */
    public String readToString(final File file) {
        try {
            return new String(Files.toByteArray(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取文件为字符串
     *
     * @param file     the file to read, must not be {@code null}
     * @param encoding the encoding to use, {@code null} means platform default
     *
     * @return the file contents, never {@code null}
     */
    public String readToString(File file, Charset encoding) {
        try {
            return new String(Files.toByteArray(file), encoding);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 读取文件为 byte 数组
     *
     * @param file the file to read, must not be {@code null}
     *
     * @return the file contents, never {@code null}
     */
    public byte[] readToByteArray(File file) {
        try {
            return Files.toByteArray(file);
        } catch (IOException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 拼接临时文件目录。
     *
     * @return 临时文件目录。
     */
    public String toTempDirPath(String subDirFile) {
        return toTempDir(subDirFile).getAbsolutePath();
    }

    /**
     * Returns a {@link File} representing the system temporary directory.
     *
     * @return the system temporary directory.
     */
    public File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * 拼接临时文件目录。
     *
     * @return 临时文件目录。
     */
    public File toTempDir(String subDirFile) {
        String tempDirPath = System.getProperty("java.io.tmpdir");
        if (subDirFile.startsWith("/")) {
            subDirFile = subDirFile.substring(1);
        }
        String fullPath = tempDirPath.concat(subDirFile);
        File fullFilePath = new File(fullPath);
        File dir = fullFilePath.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return fullFilePath;
    }

    /**
     * Copy from InputStream to OutputStream, Closes both streams when done.
     *
     * @param in  InputStream
     * @param out OutputStream
     *
     * @return the number of bytes copied
     *
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        requireNotNull(in, "No InputStream specified");
        requireNotNull(out, "No OutputStream specified");
        try {
            int byteCount = 0;
            byte[] buffer = new byte[1024 * 4];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            close(in);
            close(out);
        }
    }

    /**
     * Copy from byte array to OutputStream, Closes the stream when done.
     *
     * @param in  the byte array
     * @param out OutputStream
     *
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] in, OutputStream out) throws IOException {
        requireNotNull(in, "No input byte array specified");
        requireNotNull(out, "No OutputStream specified");
        try {
            out.write(in);
        } finally {
            close(out);
        }
    }

    /**
     * Attempt to close the supplied {@link Closeable}, ignore exceptions
     *
     * @param closeable the {@code Closeable} to close
     */
    private static void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ex) {
            // ignore
        }
    }

    // endregion

    // region DateTime

    /**
     * 日期时间格式化
     *
     * @param temporal 时间
     *
     * @return 格式化后的时间
     */

    public String formatDateTime(Temporal temporal) {
        return DateUtil.format(temporal, DateUtil.FORMATTER_YMD_HMS);
    }

    /**
     * 日期格式化
     *
     * @param temporal 时间
     *
     * @return 格式化后的时间
     */

    public String formatDate(Temporal temporal) {
        return DateUtil.format(temporal, DateUtil.FORMATTER_YMD);
    }

    /**
     * 时间格式化
     *
     * @param temporal 时间
     *
     * @return 格式化后的时间
     */

    public String formatTime(Temporal temporal) {
        return DateUtil.format(temporal, DateUtil.FORMATTER_HMS);
    }

    /**
     * 对象格式化 支持数字，date，java8 时间
     *
     * @param object  格式化对象
     * @param pattern 表达式
     *
     * @return 格式化后的字符串
     */
    public String format(Object object, String pattern) {
        if (object instanceof String str) {
            return format(str, new Object[]{pattern});
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        if (object instanceof Number) {
            DecimalFormat decimalFormat = new DecimalFormat(pattern);
            return decimalFormat.format(object);
        } else if (object instanceof TemporalAccessor accessor) {
            return formatter.format(accessor);
        } else {
            throw new IllegalArgumentException(format("Unsupported object:{},pattern:{}", object, pattern));
        }
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr 时间字符串
     * @param pattern 表达式
     *
     * @return 时间
     */
    public LocalDateTime parseDate(String dateStr, String pattern) {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr   时间字符串
     * @param formatter DateTimeFormatter
     *
     * @return 时间
     */
    public LocalDateTime parse(String dateStr, DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateStr, formatter);
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr   时间字符串
     * @param formatter DateTimeFormatter
     *
     * @return 时间
     */
    public LocalDateTime parseDateTime(String dateStr, DateTimeFormatter formatter) {
        return LocalDateTime.parse(dateStr, formatter);
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr 时间字符串
     *
     * @return 时间
     */
    public LocalDateTime parseDateTime(String dateStr) {
        return LocalDateTime.parse(dateStr, DateUtil.FORMATTER_YMD_HMS);
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr   时间字符串
     * @param formatter DateTimeFormatter
     *
     * @return 时间
     */
    public LocalDate parseDate(String dateStr, DateTimeFormatter formatter) {
        return LocalDate.parse(dateStr, formatter);
    }

    /**
     * 将字符串转换为日期
     *
     * @param dateStr 时间字符串
     *
     * @return 时间
     */
    public LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DateUtil.FORMATTER_YMD);
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr   时间字符串
     * @param formatter DateTimeFormatter
     *
     * @return 时间
     */
    public LocalTime parseTime(String dateStr, DateTimeFormatter formatter) {
        return LocalTime.parse(dateStr, formatter);
    }

    /**
     * 将字符串转换为时间
     *
     * @param dateStr 时间字符串
     *
     * @return 时间
     */
    public LocalTime parseTime(String dateStr) {
        return LocalTime.parse(dateStr, DateUtil.FORMATTER_HMS);
    }

    /**
     * 时间比较
     *
     * @param startInclusive the start instant, inclusive, not null
     * @param endExclusive   the end instant, exclusive, not null
     *
     * @return a {@code Duration}, not null
     */
    public Duration between(Temporal startInclusive, Temporal endExclusive) {
        return Duration.between(startInclusive, endExclusive);
    }

    /**
     * 比较 2 个 时间差
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     *
     * @return 时间间隔
     */
    public Duration between(Date startDate, Date endDate) {
        return Duration.between(startDate.toInstant(), endDate.toInstant());
    }

    // endregion

    // region Reflection

    /**
     * 获取方法参数信息
     *
     * @param constructor    构造器
     * @param parameterIndex 参数序号
     *
     * @return {MethodParameter}
     */
    public Parameter getMethod(Constructor<?> constructor, int parameterIndex) {
        return constructor.getParameters()[parameterIndex];
    }

    /**
     * 获取方法参数信息
     *
     * @param method         方法
     * @param parameterIndex 参数序号
     *
     * @return {MethodParameter}
     */
    public Parameter getMethodParameter(Method method, int parameterIndex) {
        return method.getParameters()[parameterIndex];
    }

    /**
     * 获取 Annotation 注解
     *
     * @param annotatedElement AnnotatedElement
     * @param annotationType   注解类
     * @param <A>              泛型标记
     *
     * @return {Annotation}
     */

    public <A extends Annotation> A getAnnotation(AnnotatedElement annotatedElement, Class<A> annotationType) {
        return annotatedElement.getDeclaredAnnotation(annotationType);
    }

    /**
     * 获取 Annotation，先找方法，没有则再找方法上的类
     *
     * @param method         Method
     * @param annotationType 注解类
     * @param <A>            泛型标记
     *
     * @return {Annotation}
     */

    public <A extends Annotation> A getAnnotation(Method method, Class<A> annotationType) {
        return defaultIfNull(method.getAnnotation(annotationType), method.getDeclaringClass().getAnnotation(annotationType));
    }

    /**
     * 实例化对象
     *
     * @param clazz 类
     * @param <T>   泛型标记
     *
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public <T> T newInstance(Class<?> clazz) {
        try {
            return (T) clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 实例化对象
     *
     * @param clazzStr 类名
     * @param <T>      泛型标记
     *
     * @return 对象
     */
    public <T> T newInstance(String clazzStr) {
        try {
            return newInstance(Class.forName(clazzStr));
        } catch (ClassNotFoundException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 获取 Bean 的属性
     *
     * @param bean         bean
     * @param propertyName 属性名
     *
     * @return 属性值
     */

    public Object getProperty(Object bean, String propertyName) {
        Class<?> beanClass = bean.getClass();
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyName, beanClass);
            Method getMethod = pd.getReadMethod();
            return getMethod.invoke(bean);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 设置 Bean 属性
     *
     * @param bean         bean
     * @param propertyName 属性名
     * @param value        属性值
     */
    public void setProperty(Object bean, String propertyName, Object value) {
        Class<?> beanClass = bean.getClass();
        try {
            // 获取属性对象
            Field declaredField = beanClass.getDeclaredField(propertyName);
            declaredField.setAccessible(true);
            // 修改属性值
            declaredField.set(bean, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 浅复制
     *
     * @param source 源对象
     * @param <T>    泛型标记
     *
     * @return T
     */

    @SuppressWarnings("unchecked")
    public <T> T clone(T source) {
        if (source == null) {
            return null;
        }
        // 1.获取字节码对象
        Class<T> clz = (Class<T>) source.getClass();
        // 2.获取实例对象（新对象）
        // 获取构造方法（保证一定能获取一个构造方法）
        Constructor<?> c = clz.getDeclaredConstructors()[0];
        // 获取构造方法的参数列表的所有类型
        Class<?>[] cs = c.getParameterTypes();
        // 新建 Object 类型的数组，存放每个参数给与的初始值
        Object[] os = new Object[cs.length];
        // 遍历数组
        for (int i = 0; i < cs.length; i++) {
            // 判断是否是基本数据类型
            if (cs[i].isPrimitive()) {
                // 基本数据类型
                if (cs[i] == byte.class || cs[i] == short.class || cs[i] == int.class || cs[i] == long.class) {
                    // 初始值赋值为 0
                    os[i] = 0;
                }
                if (cs[i] == char.class) {
                    os[i] = '\u0000';
                }
                if (cs[i] == float.class) {
                    os[i] = 0.0F;
                }
                if (cs[i] == double.class) {
                    os[i] = 0.0;
                }
                if (cs[i] == boolean.class) {
                    os[i] = false;
                }

            } else {
                // 引用数据类型
                os[i] = null;
            }
        }
        try {
            // 给定值，执行构造方法
            // 返回实例对象 //返回值有 3 个点，
            T o = (T) c.newInstance(os);
            // 3.获取原对象的所有属性 指定
            Field[] fs = clz.getDeclaredFields();

            // 4.把原对象的属性值赋值到新对象中
            for (Field f : fs) {
                // 暴力破解
                f.setAccessible(true);
                // 获取原对象的属性值
                Object value = f.get(source);
                // 把原对象的属性值赋值到新对象的属性中
                f.set(o, value);
            }
            // 返回新对象
            return o;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw Exceptions.unchecked(e);
        }
    }

    /**
     * 将对象装成 map 形式，使用反射实现，性能不好
     *
     * @param bean 源对象
     *
     * @return {Map}
     */
    public Map<String, Object> toMap(Object bean) {
        if (bean == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Field[] declaredFields = bean.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(bean));
            } catch (IllegalAccessException e) {
                throw Exceptions.unchecked(e);
            }
        }
        return map;
    }

    /**
     * 将 map 转为 bean，使用反射实现，性能不好
     *
     * @param beanMap   map
     * @param valueType 对象类型
     * @param <T>       泛型标记
     *
     * @return {T}
     */
    public <T> T toBean(Map<String, Object> beanMap, Class<T> valueType) {
        if (beanMap == null) {
            return null;
        }
        try {
            T object = valueType.getDeclaredConstructor().newInstance();
            Field[] fields = valueType.getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                    continue;
                }
                field.setAccessible(true);
                field.set(object, beanMap.get(field.getName()));
            }
            return object;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw Exceptions.unchecked(e);
        }
    }

    // endregion

}