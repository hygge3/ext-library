package ext.library.tool.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import ext.library.tool.constant.Symbol;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class ObjectUtil {

    private static final List<String> STR_TRUE = List.of("1", "true", "yes", "ok", "y");

    private static final List<String> STR_FALSE = List.of("0", "false", "no", "n");

    /**
     * 判断给定对象是否表示“真”值
     * 该方法通过检查不同类型的对象来确定它们是否代表一个“真”值
     * 对于字符串，它检查是否包含在预定义的表示真的字符串集合中
     * 对于数字，它检查值是否大于零
     * 对于布尔值，它检查是否为 true
     * 其他类型的对象默认返回 false
     *
     * @param object 要检查的对象
     *
     * @return 如果对象表示“真”值，则返回 true；否则返回 false
     */
    public static boolean isTrue(Object object) {
        // 根据对象类型判断其是否表示“真”值
        return switch (object) {
            // 如果是字符串，检查是否在表示真的字符串集合中
            case String str -> STR_TRUE.contains(str);
            // 如果是数字，检查其双精度值是否大于零
            case Number num -> num.doubleValue() > 0;
            // 如果是布尔值，检查是否为 true
            case Boolean bool -> bool;
            // 默认情况下返回 false
            default -> false;
        };
    }

    /**
     * 判断给定对象是否在某种意义上表示“假”或无效的状态
     * 此方法用于检查传递的对象是否符合特定条件，以决定其是否表示一个“假”或无效的状态
     * 它通过检查对象的类型和值来确定这一点
     *
     * @param object 要进行检查的对象，可以是任何类型
     *
     * @return 如果对象表示“假”或无效状态，则返回 true；否则返回 false
     */
    public static boolean isFalse(Object object) {
        // 根据对象类型，判断其是否表示“假”或无效状态
        return switch (object) {
            // 当对象为字符串时，检查其是否包含在预定义的表示“假”的字符串集合中
            case String str -> STR_FALSE.contains(str);
            // 当对象为数字时，检查其是否小于等于 0，因为这在许多上下文中表示失败或无效
            case Number num -> num.doubleValue() <= 0;
            // 当对象为布尔值时，直接检查其是否为 false
            case Boolean bool -> !bool;
            // 对于所有其他情况，默认返回 false，表示对象不表示“假”或无效状态
            default -> false;
        };
    }

    /**
     * 判断对象是否为 null
     *
     * @param object 要根据 {@code null} 检查的引用
     *
     * @return {@code true} 如果提供的引用为 {@code null}，否则 {@code false}
     *
     */
    public static boolean isNull(Object object) {
        return Objects.isNull(object);
    }

    /**
     * 判断对象是否不为 null
     *
     * @param object 要根据 {@code null} 检查的引用
     *
     * @return {@code true} 如果提供的引用不是 {@code null}，否则 {@code false}
     *
     */
    public static boolean isNotNull(Object object) {
        return Objects.nonNull(object);
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
    public static int size(Object obj) {
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
    public static boolean isArray(Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * 判断空对象 object、map、list、set、字符串、数组
     *
     * @param obj the object to check
     *
     * @return 数组是否为空
     */
    public static boolean isEmpty(Object obj) {
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
    public static boolean isNotEmpty(Object obj) {
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
    public static boolean equalsSafe(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 != null && o2 != null) {
            return o1.equals(o2);
        } else {
            return false;
        }
    }

    /**
     * 比较两个对象是否不相等。<br>
     *
     * @param o1 对象 1
     * @param o2 对象 2
     *
     * @return 是否不 eq
     */
    public static boolean isNotEqual(Object o1, Object o2) {
        return !Objects.equals(o1, o2);
    }


    /**
     * 如果对象为 null，返回默认值
     *
     * @param object       Object
     * @param defaultValue 默认值
     *
     * @return Object
     */
    public static <T> T defaultIfNull(T object, T defaultValue) {
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
    public static <T> T defaultIfEmpty(T object, T defaultValue) {
        return isEmpty(object) ? defaultValue : object;
    }

    /**
     * Return a String representation of the contents of the specified array.
     *
     * @param array the array to build a String representation for
     *
     * @return a String representation of {@code array}
     */
    public static String toString(Object[] array) {
        if (array == null) {
            return Symbol.NULL;
        }
        int length = array.length;
        if (length == 0) {
            return Symbol.LEFT_BRACKET + Symbol.RIGHT_BRACKET;
        }
        StringJoiner stringJoiner = new StringJoiner(Symbol.COMMA, Symbol.LEFT_BRACKET, Symbol.RIGHT_BRACKET);
        for (Object o : array) {
            stringJoiner.add(String.valueOf(o));
        }
        return stringJoiner.toString();
    }

}