package ext.library.core.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.Nullable;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 切面工具类
 */
public final class AspectUtil {

    private AspectUtil() {
    }

    /**
     * 获取切入的方法
     *
     * @param point 切面
     * @return 方法对象，非方法签名时返回 null
     */
    public static @Nullable Method getMethod(ProceedingJoinPoint point) {
        return point.getSignature() instanceof MethodSignature ms ? ms.getMethod() : null;
    }

    /**
     * 获取切入点方法上的注解，找不到则往类上找
     *
     * @param point 切面
     * @param cls   注解类型
     * @param <T>   注解泛型
     * @return 注解实例，未找到时返回 null
     */
    public static <T extends Annotation> @Nullable T getAnnotation(ProceedingJoinPoint point, Class<T> cls) {
        Method method = getMethod(point);
        T annotation = method != null ? method.getAnnotation(cls) : null;
        if (annotation != null) {
            return annotation;
        }
        Object target = point.getTarget();
        if (target == null) {
            return null;
        }
        // 必须剥掉代理：否则拿到的是 CGLIB/JDK 子类，类级注解（含 @Inherited）会被跳过。
        // JDK 动态代理不实现 TargetClassAware，AopUtils.getTargetClass 对它只会返回代理类本身，
        // 所以 Advised 必须走 advised.getTargetClass()
        Class<?> targetClass = target instanceof Advised advised
                ? advised.getTargetClass() : AopUtils.getTargetClass(target);
        return targetClass.getAnnotation(cls);
    }

}
