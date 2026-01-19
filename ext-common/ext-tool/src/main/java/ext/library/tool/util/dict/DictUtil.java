package ext.library.tool.util.dict;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
import ext.library.tool.util.ReflectionUtil;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 字典工具
 */
public final class DictUtil {

    /**
     * 获取词典列表
     *
     * @param clazz   枚举类
     * @param lambdas 获取属性方法
     *
     * @return {@code @NotNull List<Map<String, Object>> }
     */
    @SafeVarargs
    public static <D extends IDict> List<Map<String, Object>> getDictionaryList(Class<D> clazz, Function<D, Object>... lambdas) {
        D[] enumConstants = clazz.getEnumConstants();
        List<Map<String, Object>> mapList = new ArrayList<>(enumConstants.length);
        // 取出所有枚举类型
        Arrays.stream(enumConstants).forEach(enumItem -> {
            Map<String, Object> item = new HashMap<>(lambdas.length);
            // 依次取出参数的值
            Arrays.stream(lambdas).forEach(lambda -> {
                try {
                    // String prop = 从 lambda 表达式中取出属性名 并取消首字母的大写
                    String prop = StringUtils.uncapitalize(ReflectionUtil.getLambdaFunctionName(lambda));
                    item.put(prop, lambda.apply(enumItem));
                } catch (Exception exception) {
                    Logs.error(EmojiSymbol.TOOL, exception, exception.getMessage());
                }
            });
            mapList.add(item);
        });
        return mapList;
    }
}
