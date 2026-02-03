package ext.library.tool.util;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * 对象工具类
 * <p>
 * 提供对象的空判断、类型判断、大小计算等通用功能。
 *
 * @since 2025.01.01
 */
public final class ObjectUtil {

    /**
     * 表示"真"的字符串集合（不区分大小写匹配建议使用 {@link #isTrue(Object)}）
     */
    private static final List<String> STR_TRUE = List.of("1", "true", "yes", "ok", "y");

    /**
     * 表示"假"的字符串集合
     */
    private static final List<String> STR_FALSE = List.of("0", "false", "no", "n");

    private ObjectUtil() {
        // 防止实例化
    }

    // region 布尔判断

    /**
     * 判断对象是否表示"真"值
     * <ul>
     *   <li>String: 包含在 ["1", "true", "yes", "ok", "y"] 中（区分大小写）</li>
     *   <li>Number: 值大于 0</li>
     *   <li>Boolean: 为 true</li>
     *   <li>其他类型: 返回 false</li>
     * </ul>
     *
     * @param object 要检查的对象
     *
     * @return 如果对象表示"真"值返回 true，否则返回 false
     */
    public static boolean isTrue(@Nullable Object object) {
        return switch (object) {
            case String str -> STR_TRUE.contains(str.toLowerCase());
            case Number num -> num.doubleValue() > 0;
            case Boolean bool -> bool;
            case null, default -> false;
        };
    }

    /**
     * 判断对象是否表示"假"值
     * <ul>
     *   <li>String: 包含在 ["0", "false", "no", "n"] 中（区分大小写）</li>
     *   <li>Number: 值小于等于 0</li>
     *   <li>Boolean: 为 false</li>
     *   <li>其他类型: 返回 false</li>
     * </ul>
     *
     * @param object 要检查的对象
     *
     * @return 如果对象表示"假"值返回 true，否则返回 false
     */
    public static boolean isFalse(@Nullable Object object) {
        return switch (object) {
            case String str -> STR_FALSE.contains(str.toLowerCase());
            case Number num -> num.doubleValue() <= 0;
            case Boolean bool -> !bool;
            case null, default -> false;
        };
    }

    // endregion

    // region 空值判断

    /**
     * 判断对象是否为 null
     *
     * @param object 要检查的对象
     *
     * @return 如果为 null 返回 true
     *
     * @see Objects#isNull(Object)
     */
    public static boolean isNull(@Nullable Object object) {
        return object == null;
    }

    /**
     * 判断对象是否不为 null
     *
     * @param object 要检查的对象
     *
     * @return 如果不为 null 返回 true
     *
     * @see Objects#nonNull(Object)
     */
    public static boolean isNotNull(@Nullable Object object) {
        return object != null;
    }

    /**
     * 判断对象是否为空
     * <p>
     * 支持的类型：
     * <ul>
     *   <li>null: 返回 true</li>
     *   <li>Optional: 调用 isEmpty()</li>
     *   <li>CharSequence: 调用 isEmpty()</li>
     *   <li>Collection: 调用 isEmpty()</li>
     *   <li>Map: 调用 isEmpty()</li>
     *   <li>Iterable: 检查迭代器是否有下一个元素</li>
     *   <li>Iterator: 检查是否有下一个元素</li>
     *   <li>数组: 检查长度是否为 0</li>
     *   <li>其他类型: 返回 false</li>
     * </ul>
     *
     * @param obj 要检查的对象
     *
     * @return 如果对象为空返回 true
     */
    public static boolean isEmpty(@Nullable Object obj) {
        switch (obj) {
            case null -> {
                return true;
            }
            case Optional<?> optional -> {
                return optional.isEmpty();
            }
            case CharSequence cs -> {
                return cs.isEmpty();
            }
            case Collection<?> coll -> {
                return coll.isEmpty();
            }
            case Map<?, ?> map -> {
                return map.isEmpty();
            }
            case Iterable<?> iter -> {
                return !iter.iterator().hasNext();
            }
            case Iterator<?> iter -> {
                return !iter.hasNext();
            }
            default -> {
            }
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        return false;
    }

    /**
     * 判断对象是否不为空
     *
     * @param obj 要检查的对象
     *
     * @return 如果对象不为空返回 true
     *
     * @see #isEmpty(Object)
     */
    public static boolean isNotEmpty(@Nullable Object obj) {
        return !isEmpty(obj);
    }

    // endregion

    // region 类型判断

    /**
     * 判断对象是否为数组
     *
     * @param obj 要检查的对象
     *
     * @return 如果是数组返回 true
     */
    public static boolean isArray(@Nullable Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    // endregion

    // region 大小计算

    /**
     * 获取对象的元素数量或长度
     * <p>
     * 支持的类型：
     * <ul>
     *   <li>null: 返回 0</li>
     *   <li>Collection: 调用 size()</li>
     *   <li>Map: 调用 size()</li>
     *   <li>Iterable: 遍历计数</li>
     *   <li>Iterator: 遍历计数（<b>注意：会消耗迭代器</b>）</li>
     *   <li>数组: 返回数组长度</li>
     *   <li>其他类型: 返回 1</li>
     * </ul>
     *
     * @param obj 要计算大小的对象
     *
     * @return 对象的元素数量
     */
    public static int size(@Nullable Object obj) {
        switch (obj) {
            case null -> {
                return 0;
            }
            case Collection<?> coll -> {
                return coll.size();
            }
            case Map<?, ?> map -> {
                return map.size();
            }
            case Iterable<?> iter -> {
                int count = 0;
                for (Object ignored : iter) {
                    count++;
                }
                return count;
            }
            case Iterator<?> iter -> {
                int count = 0;
                while (iter.hasNext()) {
                    iter.next();
                    count++;
                }
                return count;
            }
            default -> {
            }
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj);
        }
        return 1;
    }

    // endregion

    // region 比较方法

    /**
     * 比较两个对象是否相等（null 安全）
     *
     * @param o1 第一个对象
     * @param o2 第二个对象
     *
     * @return 如果相等返回 true
     *
     * @see Objects#equals(Object, Object)
     */
    public static boolean equals(@Nullable Object o1, @Nullable Object o2) {
        return Objects.equals(o1, o2);
    }

    /**
     * 比较两个对象是否不相等
     *
     * @param o1 第一个对象
     * @param o2 第二个对象
     *
     * @return 如果不相等返回 true
     */
    public static boolean notEquals(@Nullable Object o1, @Nullable Object o2) {
        return !Objects.equals(o1, o2);
    }

    // endregion

    // region 默认值

    /**
     * 如果对象为 null，返回默认值
     *
     * @param object       对象
     * @param defaultValue 默认值
     *
     * @return 对象不为 null 时返回对象，否则返回默认值
     */
    public static <T> @Nullable T defaultIfNull(@Nullable T object, @Nullable T defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * 如果对象为空，返回默认值
     *
     * @param object       对象
     * @param defaultValue 默认值
     *
     * @return 对象不为空时返回对象，否则返回默认值
     *
     * @see #isEmpty(Object)
     */
    public static <T> @Nullable T defaultIfEmpty(@Nullable T object, @Nullable T defaultValue) {
        return isEmpty(object) ? defaultValue : object;
    }

    // endregion

    // region 字符串转换

    /**
     * 将数组转换为字符串表示
     *
     * @param array 数组
     *
     * @return 字符串表示，格式如 "[a,b,c]"
     */
    public static String toString(Object @Nullable [] array) {
        if (array == null) {
            return "null";
        }
        if (array.length == 0) {
            return "[]";
        }
        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (Object element : array) {
            joiner.add(String.valueOf(element));
        }
        return joiner.toString();
    }

    // endregion

}
