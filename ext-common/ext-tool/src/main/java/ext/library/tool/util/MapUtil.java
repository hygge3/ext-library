package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Map 工具类
 * <p>
 * 提供 Map 的判断、操作、转换等常用功能。
 *
 * @since 2025.01.01
 */
public final class MapUtil {

    private MapUtil() {
        // 防止实例化
    }

    // region 判断方法

    /**
     * 判断 Map 的 key 是否满足约束条件
     * <p>
     * 验证逻辑：
     * <ol>
     *   <li>必须包含所有 mustContainKeys</li>
     *   <li>只能包含 mustContainKeys 和 canContainKeys 中的 key</li>
     * </ol>
     *
     * @param paramMap        待验证的 Map
     * @param mustContainKeys 必须包含的 key
     * @param canContainKeys  可选包含的 key
     * @return 是否满足条件
     */
    @SafeVarargs
    public static <K> boolean isKeys(Map<K, ?> paramMap, K[] mustContainKeys, K... canContainKeys) {
        // 1. 必传参数校验
        for (K key : mustContainKeys) {
            if (!paramMap.containsKey(key)) {
                return false;
            }
        }

        // 2. 无可选参数时直接返回
        if (ObjectUtil.isEmpty(canContainKeys)) {
            return paramMap.size() == mustContainKeys.length;
        }

        // 3. 检查 Map 大小是否超过允许的最大 key 数量
        int maxKeySize = mustContainKeys.length + canContainKeys.length;
        if (paramMap.size() > maxKeySize) {
            return false;
        }

        // 4. 统计 Map 中包含的可选 key 数量
        int optionalKeyCount = 0;
        for (K key : canContainKeys) {
            if (paramMap.containsKey(key)) {
                optionalKeyCount++;
            }
        }

        // 5. 验证 Map 大小 = 必须 key 数量 + 实际可选 key 数量
        return paramMap.size() == mustContainKeys.length + optionalKeyCount;
    }

    /**
     * 判断 Map 的 key 是否与数组完全匹配
     *
     * @param paramMap 待验证的 Map
     * @param keys     期望的 key 数组
     * @return 匹配所有 key 且大小一致返回 true
     */
    public static <K> boolean isKeysEqual(Map<K, ?> paramMap, K[] keys) {
        if (paramMap.size() != keys.length) {
            return false;
        }
        for (K key : keys) {
            if (!paramMap.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断 Map 是否包含指定 key 之一
     *
     * @param paramMap 待验证的 Map
     * @param keys     key 数组
     * @return 包含任意一个 key 返回 true
     */
    public static <K> boolean containsAnyKey(Map<K, ?> paramMap, K[] keys) {
        for (K key : keys) {
            if (paramMap.containsKey(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断 Map 数组是否为空
     * <p>
     * 弱判断：只检查数组本身和第一个元素
     *
     * @param paramMaps Map 数组
     * @return 数组为 null、长度为 0 或第一个元素为空时返回 true
     */
    public static boolean isArrayEmpty(Map<?, ?> @Nullable [] paramMaps) {
        return paramMaps == null || paramMaps.length == 0
                || paramMaps[0] == null || paramMaps[0].isEmpty();
    }

    /**
     * 判断 Map 中是否存在空白字符串值
     *
     * @param paramMap 待检查的 Map
     * @return Map 为空或包含空白字符串值时返回 true
     */
    public static boolean hasBlankStringValue(Map<?, ?> paramMap) {
        if (paramMap.isEmpty()) {
            return true;
        }
        for (Object value : paramMap.values()) {
            if (value instanceof String str && str.isBlank()) {
                return true;
            }
        }
        return false;
    }

    // endregion

    // region 操作方法

    /**
     * 批量移除指定的 key
     *
     * @param map  待操作的 Map
     * @param keys 要移除的 key
     * @return 修改后的 Map（同一实例）
     */
    @SafeVarargs
    public static <K, V> Map<K, V> removeKeys(Map<K, V> map, K... keys) {
        for (K key : keys) {
            map.remove(key);
        }
        return map;
    }

    /**
     * 移除值为 null 的条目
     *
     * @param paramMap 待操作的 Map
     */
    public static void removeNullValues(Map<?, ?> paramMap) {
        paramMap.entrySet().removeIf(entry -> entry.getValue() == null);
    }

    /**
     * 移除值为 null 或空白字符串的条目
     *
     * @param paramMap 待操作的 Map
     */
    public static void removeBlankValues(Map<?, ?> paramMap) {
        paramMap.entrySet().removeIf(entry -> {
            Object value = entry.getValue();
            return value == null || (value instanceof String str && str.isBlank());
        });
    }

    /**
     * 对所有字符串值进行 trim 操作
     *
     * @param paramMap 待操作的 Map
     */
    public static <K> void trimStringValues(Map<K, String> paramMap) {
        paramMap.replaceAll((key, value) -> value != null ? value.trim() : null);
    }

    /**
     * 替换 key（保留原值）
     *
     * @param paramMap   待操作的 Map
     * @param oldKey     原 key
     * @param newKey     新 key
     */
    public static <K, V> void replaceKey(Map<K, V> paramMap, K oldKey, K newKey) {
        if (paramMap.containsKey(oldKey)) {
            paramMap.put(newKey, paramMap.remove(oldKey));
        }
    }

    // endregion

    // region 获取方法

    /**
     * 获取所有 key 的列表
     *
     * @param paramMap Map
     * @return key 列表
     */
    public static <K> List<K> keyList(Map<K, ?> paramMap) {
        return new ArrayList<>(paramMap.keySet());
    }

    /**
     * 安全获取并转换 Map 中的值
     *
     * @param paramMap Map
     * @param key      key
     * @param clazz    目标类型
     * @return 转换后的值，不存在时返回 null
     */
    public static <K, T> @Nullable T getObject(Map<?, ?> paramMap, K key, Class<T> clazz) {
        Object value = paramMap.get(key);
        return value != null ? TypeCastUtil.cast(value, clazz) : null;
    }

    // endregion

    // region 转换方法

    /**
     * 将列表按指定 key 分组
     *
     * @param list          列表
     * @param keyClassifier key 提取函数
     * @return 分组后的 Map
     */
    public static <K, T> Map<K, List<T>> groupBy(List<T> list, Function<T, K> keyClassifier) {
        if (ObjectUtil.isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.groupingBy(keyClassifier));
    }

    /**
     * 将列表转换为 Map（提取 key 和 value）
     * <p>
     * 注意：相同 key 时后面的值会覆盖前面的值
     *
     * @param list            列表
     * @param keyClassifier   key 提取函数
     * @param valueClassifier value 提取函数
     * @return 转换后的 Map（保持插入顺序）
     */
    public static <K, T, V> Map<K, V> toMap(List<T> list, Function<T, K> keyClassifier, Function<T, V> valueClassifier) {
        if (ObjectUtil.isEmpty(list)) {
            return new LinkedHashMap<>();
        }
        Map<K, V> map = new LinkedHashMap<>(list.size());
        for (T item : list) {
            map.put(keyClassifier.apply(item), valueClassifier.apply(item));
        }
        return map;
    }

    /**
     * 将列表转换为 Map（以元素本身为 value）
     * <p>
     * 注意：相同 key 时后面的值会覆盖前面的值
     *
     * @param list          列表
     * @param keyClassifier key 提取函数
     * @return 转换后的 Map（保持插入顺序）
     */
    public static <K, T> Map<K, T> toMap(List<T> list, Function<T, K> keyClassifier) {
        return toMap(list, keyClassifier, Function.identity());
    }

    /**
     * 将 Bean 转换为 Map
     * <p>
     * 使用反射实现，不包含 static 和 final 字段
     *
     * @param bean 源对象
     * @return 字段名到字段值的 Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        Map<String, Object> map = new HashMap<>(fields.length);
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                continue;
            }
            field.setAccessible(true);
            try {
                map.put(field.getName(), field.get(bean));
            } catch (IllegalAccessException e) {
                throw new ToolException(EmojiSymbol.TOOL, e);
            }
        }
        return map;
    }

    /**
     * 将 Map 转换为 Bean
     * <p>
     * 使用反射实现，要求 Bean 有无参构造函数
     *
     * @param beanMap   源 Map
     * @param beanClass 目标类型
     * @return Bean 实例
     */
    public static <T> T mapToBean(Map<String, Object> beanMap, Class<T> beanClass) {
        try {
            T instance = beanClass.getDeclaredConstructor().newInstance();
            Field[] fields = beanClass.getDeclaredFields();
            for (Field field : fields) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }
                Object value = beanMap.get(field.getName());
                if (value != null) {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
            }
            return instance;
        } catch (NoSuchMethodException | InvocationTargetException
                 | InstantiationException | IllegalAccessException e) {
            throw new ToolException(EmojiSymbol.TOOL, e);
        }
    }

    // endregion

}
