package ext.library.core.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.Nullable;

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
        return annotation != null ? annotation : point.getTarget().getClass().getAnnotation(cls);
    }

}
