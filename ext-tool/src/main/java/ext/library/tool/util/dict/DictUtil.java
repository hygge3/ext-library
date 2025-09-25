package ext.library.tool.util.dict;

import ext.library.tool.util.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 字典工具
 */
public class DictUtil {
    private static final Logger log = LoggerFactory.getLogger(DictUtil.class);

    /**
     * 获取词典列表
     *
     * @param clazz   枚举类
     * @param lambdas 获取属性方法
     *
     * @return {@code @NotNull List<Map<String, Object>> }
     */
    @SafeVarargs
    public static <D extends IDict> List<Map<String, Object>> getDictionaryList(@Nonnull Class<D> clazz, Function<D, Object>... lambdas) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        // 取出所有枚举类型
        Arrays.stream(clazz.getEnumConstants()).forEach(enumItem -> {
            Map<String, Object> item = new HashMap<>(lambdas.length);
            // 依次取出参数的值
            Arrays.stream(lambdas).forEach(lambda -> {
                try {
                    // String prop = 从 lambda 表达式中取出属性名 并取消首字母的大写
                    String prop = StringUtils.uncapitalize(ReflectionUtil.getLambdaFunctionName(lambda));
                    item.put(prop, lambda.apply(enumItem));
                } catch (Exception exception) {
                    log.error(exception.getMessage(), exception);
                }
            });
            mapList.add(item);
        });
        return mapList;
    }
}