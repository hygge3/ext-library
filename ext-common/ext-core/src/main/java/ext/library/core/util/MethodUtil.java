package ext.library.core.util;

import ext.library.tool.holder.Lazy;
import org.jspecify.annotations.Nullable;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.SynthesizingMethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 方法工具类
 *
 * @since 2025.08.19
 */
public final class MethodUtil {

    private static final Lazy<ParameterNameDiscoverer> PARAMETER_NAME_DISCOVERER =
            Lazy.of(DefaultParameterNameDiscoverer::new);

    private MethodUtil() {
    }

    /**
     * 获取方法参数信息
     *
     * @param constructor    构造器
     * @param parameterIndex 参数序号
     * @return {MethodParameter}
     */
    public static MethodParameter getMethodParameter(Constructor<?> constructor, int parameterIndex) {
        MethodParameter methodParameter = new SynthesizingMethodParameter(constructor, parameterIndex);
        methodParameter.initParameterNameDiscovery(PARAMETER_NAME_DISCOVERER.get());
        return methodParameter;
    }

    /**
     * 获取方法参数信息
     *
     * @param method         方法
     * @param parameterIndex 参数序号
     * @return {MethodParameter}
     */
    public static MethodParameter getMethodParameter(Method method, int parameterIndex) {
        MethodParameter methodParameter = new SynthesizingMethodParameter(method, parameterIndex);
        methodParameter.initParameterNameDiscovery(PARAMETER_NAME_DISCOVERER.get());
        return methodParameter;
    }

    /**
     * 获取注解（支持组合注解）
     * <p>
     * 查找顺序：先查方法上的注解，找不到则查类上的注解
     *
     * @param method         方法
     * @param annotationType 注解类
     * @param <A>            泛型标记
     * @return 注解实例，未找到时返回 null
     */
    public static <A extends Annotation> @Nullable A getAnnotation(Method method, Class<A> annotationType) {
        // 处理泛型参数的桥接方法，获取原始方法
        Method resolvedMethod = BridgeMethodResolver.findBridgedMethod(method);
        // 先查方法上的注解（支持组合注解）
        A annotation = AnnotatedElementUtils.findMergedAnnotation(resolvedMethod, annotationType);
        if (annotation != null) {
            return annotation;
        }
        // 方法上找不到，则查类上的注解（支持组合注解）
        return AnnotatedElementUtils.findMergedAnnotation(resolvedMethod.getDeclaringClass(), annotationType);
    }

}
