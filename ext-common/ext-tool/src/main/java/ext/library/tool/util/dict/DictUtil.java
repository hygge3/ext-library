package ext.library.tool.util.dict;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
import ext.library.tool.util.ReflectionUtil;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

/**
 * 字典工具类
 * <p>
 * 提供枚举字典转换功能，将实现 {@link Dict} 接口的枚举转换为前端可用的字典列表。
 *
 * @since 2025.01.01
 */
public final class DictUtil {

    private DictUtil() {
    }

    /**
     * 获取枚举字典列表
     * <p>
     * 将枚举常量转换为 Map 列表，每个 Map 包含指定 lambda 对应的属性键值对。
     * 属性名从 lambda 方法引用中提取（如 {@code Dict::getKey} 提取为 "key"）。
     *
     * @param clazz   枚举类（必须实现 {@link Dict} 接口）
     * @param lambdas 属性获取方法引用
     * @param <D>     枚举类型
     * @return 字典列表，每个元素为包含指定属性的 Map
     */
    @SafeVarargs
    public static <D extends Dict> List<Map<String, Object>> getDictionaryList(
            Class<D> clazz, Function<D, Object>... lambdas) {
        D[] enumConstants = clazz.getEnumConstants();
        List<Map<String, Object>> result = new ArrayList<>(enumConstants.length);

        for (D enumItem : enumConstants) {
            Map<String, Object> item = new HashMap<>(lambdas.length);
            for (Function<D, Object> lambda : lambdas) {
                try {
                    String methodName = ReflectionUtil.getLambdaMethodName(lambda);
                    String prop = extractPropertyName(methodName);
                    item.put(prop, lambda.apply(enumItem));
                } catch (Exception e) {
                    Logs.error(EmojiSymbol.TOOL, e, "获取字典属性失败");
                }
            }
            result.add(item);
        }
        return result;
    }

    /**
     * 从方法名提取属性名
     * <p>
     * 支持 JavaBean 规范的 getter 方法名：
     * <ul>
     *   <li>{@code getValue} -> {@code value}</li>
     *   <li>{@code isActive} -> {@code active}</li>
     *   <li>{@code name} -> {@code name}（非标准 getter）</li>
     * </ul>
     *
     * @param methodName 方法名
     * @return 属性名
     */
    private static String extractPropertyName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return StringUtils.uncapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return StringUtils.uncapitalize(methodName.substring(2));
        }
        return methodName;
    }
}
