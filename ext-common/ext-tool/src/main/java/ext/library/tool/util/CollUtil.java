package ext.library.tool.util;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 集合工具类
 * <p>
 * 提供数组、集合、迭代器的常用操作，包括元素查找、数组拼接、分片等功能。
 *
 * @since 2025.01.01
 */
public final class CollUtil {

    private CollUtil() {
    }

    // region 元素查找

    /**
     * 判断数组中是否包含指定元素
     *
     * @param array   数组（可为 null）
     * @param element 要查找的元素
     * @param <T>     元素类型
     *
     * @return 如果找到返回 true，否则返回 false
     */
    public static <T> boolean contains(T @Nullable [] array, final T element) {
        if (array == null) {
            return false;
        }
        return Arrays.stream(array).anyMatch(x -> ObjectUtil.equals(x, element));
    }

    /**
     * 判断迭代器中是否包含指定元素
     *
     * @param iterator 迭代器（可为 null）
     * @param element  要查找的元素
     *
     * @return 如果找到返回 true，否则返回 false
     */
    public static boolean contains(@Nullable Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (ObjectUtil.equals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断枚举中是否包含指定元素
     *
     * @param enumeration 枚举（可为 null）
     * @param element     要查找的元素
     *
     * @return 如果找到返回 true，否则返回 false
     */
    public static boolean contains(@Nullable Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectUtil.equals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    // endregion

    // region 数组操作

    /**
     * 拼接两个字符串数组
     *
     * @param one   第一个数组
     * @param other 第二个数组
     *
     * @return 拼接后的新数组
     */
    public static String[] concat(String[] one, String[] other) {
        return concat(one, other, String.class);
    }

    /**
     * 拼接两个数组
     *
     * @param one   第一个数组
     * @param other 第二个数组
     * @param clazz 数组元素类型
     * @param <T>   元素类型
     *
     * @return 拼接后的新数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] concat(T[] one, T[] other, Class<T> clazz) {
        T[] target = (T[]) Array.newInstance(clazz, one.length + other.length);
        System.arraycopy(one, 0, target, 0, one.length);
        System.arraycopy(other, 0, target, one.length, other.length);
        return target;
    }

    // endregion

    // region 集合转换

    /**
     * 将 Iterable 转换为 List 集合
     *
     * @param elements 可迭代对象
     * @param <E>      元素类型
     *
     * @return 列表集合
     */
    public static <E> List<E> toList(Iterable<E> elements) {
        Objects.requireNonNull(elements, "元素为 null");
        if (elements instanceof Collection<E> collection) {
            return new ArrayList<>(collection);
        }
        Iterator<E> iterator = elements.iterator();
        List<E> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    /**
     * 将键值对数组转换为 Map
     * <p>
     * 数组格式为 [key1, value1, key2, value2, ...]，长度必须为偶数。
     *
     * @param keysValues 键值对数组
     * @param <K>        键类型
     * @param <V>        值类型
     *
     * @return Map 集合
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> toMap(Object... keysValues) {
        int kvLength = keysValues.length;
        Assert.isTrue(kvLength % 2 == 0, "Map 的参数数量错误，keysValues 长度不能为奇数");
        Map<K, V> keyValueMap = HashMap.newHashMap(kvLength / 2);
        for (int i = kvLength - 2; i >= 0; i -= 2) {
            Object key = keysValues[i];
            Object value = keysValues[i + 1];
            keyValueMap.put((K) key, (V) value);
        }
        return keyValueMap;
    }

    // endregion

    // region 集合操作

    /**
     * 列表分片
     * <p>
     * 将列表按指定大小分割成多个子列表。
     *
     * @param list 原始列表
     * @param size 每个分片的大小（必须大于 0）
     * @param <T>  元素类型
     *
     * @return 分片后的列表
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        Assert.isTrue(size > 0, "列表的分片大小必须大于零");
        return IntStream.range(0, (list.size() + size - 1) / size)
                .mapToObj(i -> list.subList(i * size, Math.min((i + 1) * size, list.size())))
                .toList();
    }

    /**
     * 统计集合中各元素的出现次数
     *
     * @param coll 集合
     * @param <T>  元素类型
     *
     * @return 元素到出现次数的映射
     */
    public static <T> Map<T, Long> counting(final Collection<T> coll) {
        return coll.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    // endregion
}
