package ext.library.tool.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import lombok.experimental.UtilityClass;

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
import java.util.Set;

@UtilityClass
public class CollectionUtil {
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
        return Arrays.stream(array).anyMatch(x -> ObjectUtil.equalsSafe(x, element));
    }

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
                if (ObjectUtil.equalsSafe(candidate, element)) {
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
                if (ObjectUtil.equalsSafe(candidate, element)) {
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

    /**
     * 计数
     *
     * @param coll coll
     *
     * @return {@link Multiset }<{@link T }>
     */
    public static <T> Multiset<T> counting(final Collection<T> coll) {
        return HashMultiset.create(coll);
    }

}