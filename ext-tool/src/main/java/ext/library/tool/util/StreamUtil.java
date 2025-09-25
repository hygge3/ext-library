package ext.library.tool.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import ext.library.tool.constant.Symbol;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * stream 流工具类
 */
public class StreamUtil {

    /**
     * 将 collection 过滤
     *
     * @param collection 需要转化的集合
     * @param function   过滤方法
     *
     * @return 过滤后的 list
     */
    public static <E> List<E> filter(Collection<E> collection, Predicate<E> function) {
        if (ObjectUtil.isEmpty(collection)) {
            return Lists.newArrayList();
        }
        // 注意此处不要使用 .toList() 新语法 因为返回的是不可变 List 会导致序列化问题
        return collection.stream().filter(function).collect(Collectors.toList());
    }

    /**
     * 将 collection 拼接
     *
     * @param collection 需要转化的集合
     * @param function   拼接方法
     *
     * @return 拼接后的 list
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function) {
        return join(collection, function, Symbol.COMMA);
    }

    /**
     * 将 collection 拼接
     *
     * @param collection 需要转化的集合
     * @param function   拼接方法
     * @param delimiter  拼接符
     *
     * @return 拼接后的 list
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function, CharSequence delimiter) {
        if (ObjectUtil.isEmpty(collection)) {
            return Symbol.EMPTY;
        }
        return collection.stream().map(function).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }

    /**
     * 将 collection 排序
     *
     * @param collection 需要转化的集合
     * @param comparing  排序方法
     *
     * @return 排序后的 list
     */
    public static <E> List<E> sorted(Collection<E> collection, Comparator<E> comparing) {
        if (ObjectUtil.isEmpty(collection)) {
            return Lists.newArrayList();
        }
        // 注意此处不要使用 .toList() 新语法 因为返回的是不可变 List 会导致序列化问题
        return collection.stream().filter(Objects::nonNull).sorted(comparing).collect(Collectors.toList());
    }

    /**
     * 将 collection 转化为类型不变的 map<br>
     * <B>{@code Collection<V>  ---->  Map<K,V>}</B>
     *
     * @param collection 需要转化的集合
     * @param key        V 类型转化为 K 类型的 lambda 方法
     * @param <V>        collection 中的泛型
     * @param <K>        map 中的 key 类型
     *
     * @return 转化后的 map
     */
    public static <V, K> Map<K, V> toIdentityMap(Collection<V> collection, Function<V, K> key) {
        if (ObjectUtil.isEmpty(collection)) {
            return Maps.newHashMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.toMap(key, Function.identity(), (l, r) -> l));
    }

    /**
     * 将 Collection 转化为 map(value 类型与 collection 的泛型不同)<br>
     * <B>{@code Collection<E> -----> Map<K,V>  }</B>
     *
     * @param collection 需要转化的集合
     * @param key        E 类型转化为 K 类型的 lambda 方法
     * @param value      E 类型转化为 V 类型的 lambda 方法
     * @param <E>        collection 中的泛型
     * @param <K>        map 中的 key 类型
     * @param <V>        map 中的 value 类型
     *
     * @return 转化后的 map
     */
    public static <E, K, V> Map<K, V> toMap(Collection<E> collection, Function<E, K> key, Function<E, V> value) {
        if (ObjectUtil.isEmpty(collection)) {
            return Maps.newHashMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.toMap(key, value, (l, r) -> l));
    }

    /**
     * 将 collection 按照规则 (比如有相同的班级 id) 分类成 map<br>
     * <B>{@code Collection<E> -------> Map<K,List<E>> } </B>
     *
     * @param collection 需要分类的集合
     * @param key        分类的规则
     * @param <E>        collection 中的泛型
     * @param <K>        map 中的 key 类型
     *
     * @return 分类后的 map
     */
    public static <E, K> Map<K, List<E>> groupByKey(Collection<E> collection, Function<E, K> key) {
        if (ObjectUtil.isEmpty(collection)) {
            return Maps.newHashMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 将 collection 按照两个规则 (比如有相同的年级 id，班级 id) 分类成双层 map<br>
     * <B>{@code Collection<E>  --->  Map<T,Map<U,List<E>>> } </B>
     *
     * @param collection 需要分类的集合
     * @param key1       第一个分类的规则
     * @param key2       第二个分类的规则
     * @param <E>        集合元素类型
     * @param <K>        第一个 map 中的 key 类型
     * @param <U>        第二个 map 中的 key 类型
     *
     * @return 分类后的 map
     */
    public static <E, K, U> Map<K, Map<U, List<E>>> groupBy2Key(Collection<E> collection, Function<E, K> key1, Function<E, U> key2) {
        if (ObjectUtil.isEmpty(collection)) {
            return Maps.newHashMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(key1, LinkedHashMap::new, Collectors.groupingBy(key2, LinkedHashMap::new, Collectors.toList())));
    }

    /**
     * 将 collection 按照两个规则 (比如有相同的年级 id，班级 id) 分类成双层 map<br>
     * <B>{@code Collection<E>  --->  Map<T,Map<U,E>> } </B>
     *
     * @param collection 需要分类的集合
     * @param key1       第一个分类的规则
     * @param key2       第二个分类的规则
     * @param <T>        第一个 map 中的 key 类型
     * @param <U>        第二个 map 中的 key 类型
     * @param <E>        collection 中的泛型
     *
     * @return 分类后的 map
     */
    public static <E, T, U> Map<T, Map<U, E>> group2Map(Collection<E> collection, Function<E, T> key1, Function<E, U> key2) {
        if (ObjectUtil.isEmpty(collection) || key1 == null || key2 == null) {
            return Maps.newHashMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(key1, LinkedHashMap::new, Collectors.toMap(key2, Function.identity(), (l, r) -> l)));
    }

    /**
     * 将 collection 转化为 List 集合，但是两者的泛型不同<br>
     * <B>{@code Collection<E>  ------>  List<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection 中的泛型转化为 list 泛型的 lambda 表达式
     * @param <E>        collection 中的泛型
     * @param <T>        List 中的泛型
     *
     * @return 转化后的 list
     */
    public static <E, T> List<T> toList(Collection<E> collection, Function<E, T> function) {
        if (ObjectUtil.isEmpty(collection)) {
            return Lists.newArrayList();
        }
        return collection.stream().map(function).filter(Objects::nonNull)
                // 注意此处不要使用 .toList() 新语法 因为返回的是不可变 List 会导致序列化问题
                .collect(Collectors.toList());
    }

    /**
     * 将 collection 转化为 Set 集合，但是两者的泛型不同<br>
     * <B>{@code Collection<E>  ------>  Set<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection 中的泛型转化为 set 泛型的 lambda 表达式
     * @param <E>        collection 中的泛型
     * @param <T>        Set 中的泛型
     *
     * @return 转化后的 Set
     */
    public static <E, T> Set<T> toSet(Collection<E> collection, Function<E, T> function) {
        if (ObjectUtil.isEmpty(collection) || function == null) {
            return Sets.newHashSet();
        }
        return collection.stream().map(function).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    /**
     * 合并两个相同 key 类型的 map
     *
     * @param map1  第一个需要合并的 map
     * @param map2  第二个需要合并的 map
     * @param merge 合并的 lambda，将 key value1 value2 合并成最终的类型，注意 value 可能为空的情况
     * @param <K>   map 中的 key 类型
     * @param <X>   第一个 map 的 value 类型
     * @param <Y>   第二个 map 的 value 类型
     * @param <V>   最终 map 的 value 类型
     *
     * @return 合并后的 map
     */
    public static <K, X, Y, V> Map<K, V> merge(Map<K, X> map1, Map<K, Y> map2, BiFunction<X, Y, V> merge) {
        if (ObjectUtil.isEmpty(map1) && ObjectUtil.isEmpty(map2)) {
            return Maps.newHashMap();
        } else if (ObjectUtil.isEmpty(map1)) {
            map1 = Maps.newHashMap();
        } else if (ObjectUtil.isEmpty(map2)) {
            map2 = Maps.newHashMap();
        }
        Set<K> key = Sets.newHashSet();
        key.addAll(map1.keySet());
        key.addAll(map2.keySet());
        Map<K, V> map = Maps.newHashMap();
        for (K t : key) {
            X x = map1.get(t);
            Y y = map2.get(t);
            V z = merge.apply(x, y);
            if (z != null) {
                map.put(t, z);
            }
        }
        return map;
    }

}