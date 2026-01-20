package ext.library.core.util.spel;

import ext.library.tool.holder.Lazy;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.List;

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
     *
     * @param rootObject 根对象，method 所在的对象
     * @param method     目标方法
     * @param args       方法实际入参
     * @return SpEL 上下文
     */
    public static StandardEvaluationContext getSpelContext(Object rootObject, Method method, Object[] args) {
        return new MethodBasedEvaluationContext(rootObject, method, args, PARAMETER_NAME_DISCOVERER.get());
    }

    /**
     * 解析 SpEL 表达式
     *
     * @param context        SpEL 上下文
     * @param spelExpression SpEL 表达式
     * @return 解析后的字符串
     */
    public static String parseValueToString(StandardEvaluationContext context, String spelExpression) {
        return PARSER.get().parseExpression(spelExpression).getValue(context, String.class);
    }

    /**
     * 解析 SpEL 表达式为字符串列表
     *
     * @param context        SpEL 上下文
     * @param spelExpression SpEL 表达式
     * @return 解析后的 List&lt;String&gt;
     */
    @SuppressWarnings("unchecked")
    public static List<String> parseValueToStringList(StandardEvaluationContext context, String spelExpression) {
        return (List<String>) PARSER.get().parseExpression(spelExpression).getValue(context, List.class);
    }

}
