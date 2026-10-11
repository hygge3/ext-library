package ext.library.web.util;

import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

/**
 * HandlerMethod 工具类
 * <p>
 * 提供 Spring MVC HandlerMethod 相关的注解查找功能
 *
 * @since 2026.01.20
 */
public final class HandlerMethodUtil {

    private HandlerMethodUtil() {
    }

    /**
     * 获取注解（支持组合注解）
     * <p>
     * 查找顺序：先查方法上的注解，找不到则查类上的注解
     *
     * @param handlerMethod  HandlerMethod
     * @param annotationType 注解类
     * @param <A>            泛型标记
     *
     * @return 注解实例，未找到时返回 null
     */
    public static <A extends Annotation> @Nullable A getAnnotation(HandlerMethod handlerMethod, Class<A> annotationType) {
        // 先查方法上的注解（支持组合注解）
        A annotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), annotationType);
        if (annotation != null) {
            return annotation;
        }
        // 方法上找不到，则查类上的注解（支持组合注解）
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), annotationType);
    }

}
