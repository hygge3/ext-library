package ext.library.core.util.spel;

import ext.library.tool.holder.Lazy;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpEL 表达式工具类
 */
public final class SpelUtil {

    private SpelUtil() {
    }

    /** SpEL 解析器 */
    private static final Lazy<ExpressionParser> PARSER = Lazy.of(SpelExpressionParser::new);

    /** 方法参数名发现器 */
    private static final Lazy<ParameterNameDiscoverer> PARAMETER_NAME_DISCOVERER =
            Lazy.of(StandardReflectionParameterNameDiscoverer::new);

    /**
     * 已解析表达式缓存
     * <p>
     * SpEL 解析是本工具类中最贵的操作，而表达式通常是注解/配置里的固定字符串，必须缓存。
     * 上限 1000 条：超过后不再缓存新表达式（退化为每次重新解析），避免热刷新的表达式把内存吃光。
     */
    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>();
    private static final int EXPRESSION_CACHE_LIMIT = 1000;

    /**
     * 支持 #p0 参数索引的表达式解析
     *
     * @param rootObject     根对象，method 所在类的对象实例
     * @param method         目标方法
     * @param args           方法入参
     * @param spelExpression SpEL 表达式
     * @return 解析后的字符串
     */
    public static String parseValueToString(Object rootObject, Method method, Object[] args, String spelExpression) {
        StandardEvaluationContext context = getSpelContext(rootObject, method, args);
        return parseValueToString(context, spelExpression);
    }

    /**
     * 创建 SpEL 上下文
     * <p>
     * 支持 #p0 参数索引和 #paramName 参数名访问
     * <p>
     * 上下文已锁定：禁用类型引用（{@code T(...)}）、禁用构造器（{@code new ...}），
     * 避免表达式变成远程代码执行入口。
     *
     * @param rootObject 根对象，method 所在的对象
     * @param method     目标方法
     * @param args       方法实际入参
     * @return SpEL 上下文
     */
    public static StandardEvaluationContext getSpelContext(Object rootObject, Method method, Object[] args) {
        return SpelLock.lock(new MethodBasedEvaluationContext(
                rootObject, method, args, PARAMETER_NAME_DISCOVERER.get()));
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param context        SpEL 上下文
     * @param spelExpression SpEL 表达式
     * @return 解析后的字符串
     */
    public static String parseValueToString(StandardEvaluationContext context, String spelExpression) {
        return expression(spelExpression).getValue(context, String.class);
    }

    /**
     * 解析 SpEL 表达式为字符串列表
     *
     * @param context        SpEL 上下文
     * @param spelExpression SpEL 表达式
     * @return SpEL 表达式解析结果列表，解析结果为 null 或非 List 时返回空列表
     */
    public static List<String> parseValueToStringList(StandardEvaluationContext context, String spelExpression) {
        Object value = expression(spelExpression).getValue(context, List.class);
        if (!(value instanceof List<?> list) || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .toList();
    }

    /**
     * 获取已缓存的表达式
     * <p>
     * 超上限后不再缓存新表达式（退化为每次重新解析），避免热刷新的表达式把内存吃光。
     *
     * @param spelExpression SpEL 表达式
     * @return 已编译的表达式
     */
    private static Expression expression(String spelExpression) {
        Expression cached = EXPRESSION_CACHE.get(spelExpression);
        if (cached != null) {
            return cached;
        }
        Expression parsed = PARSER.get().parseExpression(spelExpression);
        if (EXPRESSION_CACHE.size() < EXPRESSION_CACHE_LIMIT) {
            EXPRESSION_CACHE.putIfAbsent(spelExpression, parsed);
        }
        return parsed;
    }

}
