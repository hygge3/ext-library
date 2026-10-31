package ext.library.core.util.spel;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存 SpEL 表达式求值器
 * <p>
 * 通过缓存已解析的表达式和方法信息来提高性能
 */
public class ExtExpressionEvaluator extends CachedExpressionEvaluator {

    /** 缓存上限：类加载器泄漏的护栏，超过后不再缓存 */
    private static final int CACHE_LIMIT = 1024;

    /** 表达式缓存 */
    private final Map<ExpressionKey, Expression> expressionCache = new ConcurrentHashMap<>(64);

    /** 方法缓存 */
    private final Map<AnnotatedElementKey, Method> methodCache = new ConcurrentHashMap<>(64);

    /**
     * 创建求值上下文 (自动构建 RootObject)
     *
     * @param method      方法
     * @param args        方法参数
     * @param target      目标对象实例
     * @param targetClass 目标类
     * @param beanFactory Bean 工厂 (可选)
     * @return 求值上下文
     */
    public EvaluationContext createContext(Method method, Object[] args, Object target,
                                           Class<?> targetClass, BeanFactory beanFactory) {
        Method targetMethod = getTargetMethod(targetClass, method);
        ExtExpressionRootObject rootObject = new ExtExpressionRootObject(
                method, args, target, targetClass, targetMethod);
        return buildEvaluationContext(targetMethod, args, rootObject, beanFactory);
    }

    /**
     * 创建求值上下文 (自定义 RootObject)
     *
     * @param method      方法
     * @param args        方法参数
     * @param targetClass 目标类
     * @param rootObject  自定义根对象
     * @param beanFactory Bean 工厂 (可选)
     * @return 求值上下文
     */
    public EvaluationContext createContext(Method method, Object[] args, Class<?> targetClass,
                                           Object rootObject, BeanFactory beanFactory) {
        Method targetMethod = getTargetMethod(targetClass, method);
        return buildEvaluationContext(targetMethod, args, rootObject, beanFactory);
    }

    /**
     * 求值表达式
     */
    public Object eval(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return eval(expression, methodKey, evalContext, null);
    }

    /**
     * 求值表达式并转换为指定类型
     */
    public <T> T eval(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext,
                      Class<T> valueType) {
        return getExpression(this.expressionCache, methodKey, expression).getValue(evalContext, valueType);
    }

    /**
     * 求值表达式为字符串
     */
    public String evalAsText(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return eval(expression, methodKey, evalContext, String.class);
    }

    /**
     * 求值表达式为布尔值
     */
    public boolean evalAsBool(String expression, AnnotatedElementKey methodKey, EvaluationContext evalContext) {
        return Boolean.TRUE.equals(eval(expression, methodKey, evalContext, Boolean.class));
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        this.expressionCache.clear();
        this.methodCache.clear();
    }

    private EvaluationContext buildEvaluationContext(Method targetMethod, Object[] args,
                                                     Object rootObject, BeanFactory beanFactory) {
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                rootObject, targetMethod, args, getParameterNameDiscoverer());
        if (beanFactory != null) {
            context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        // 加固：禁用类型引用与构造器解析，避免来自配置的表达式变成 RCE 入口
        return SpelLock.lock(context);
    }

    private Method getTargetMethod(Class<?> targetClass, Method method) {
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, targetClass);
        Method cached = methodCache.get(methodKey);
        if (cached != null) {
            return cached;
        }
        Method resolved = AopUtils.getMostSpecificMethod(method, targetClass);
        // 缓存的键强引用 Class/Method，热部署或反复建 context 时旧 ClassLoader 永远无法回收，故设上限
        if (methodCache.size() < CACHE_LIMIT) {
            methodCache.putIfAbsent(methodKey, resolved);
        }
        return resolved;
    }

}
