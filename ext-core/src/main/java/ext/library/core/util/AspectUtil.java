package ext.library.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * 切面工具类
 */
@UtilityClass
public final class AspectUtil {

    /**
     * 获取切入的方法
     *
     * @param point 切面
     * @return java.lang.reflect.Method
     */
    public Method getMethod(ProceedingJoinPoint point) {
        if (point.getSignature() instanceof MethodSignature ms) {
            return ms.getMethod();
        }
        return null;
    }

    /**
     * 获取切入点方法上的注解，找不到则往类上找
     *
     * @param point 切面
     * @param cls   注解类型
     * @return T 注解类型
     */
    public <T extends Annotation> T getAnnotation(ProceedingJoinPoint point, Class<T> cls) {
        Method method = getMethod(point);
        T t = null;
        if (method != null) {
            t = method.getAnnotation(cls);
        }

        if (t == null) {
            t = point.getTarget().getClass().getAnnotation(cls);
        }
        return t;
    }

}
