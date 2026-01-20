package ext.library.web.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HandlerMethodUtil 工具类测试
 */
@DisplayName("HandlerMethodUtil 工具类测试")
class HandlerMethodUtilTest {

    // ========== 测试注解 ==========

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value() default "";
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface OtherAnnotation {
    }

    @TestAnnotation("类级别注解")
    static class TestService {

        @TestAnnotation("方法级别注解")
        public String annotatedMethod(String param) {
            return "result: " + param;
        }

        public String nonAnnotatedMethod(String param) {
            return "result: " + param;
        }

        public void plainMethod() {
        }
    }

    static class NoAnnotationService {
        public void method() {
        }
    }

    // ========== getAnnotation(HandlerMethod, Class) 测试 ==========

    @Test
    @DisplayName("测试从 HandlerMethod 获取方法级别注解")
    void testGetHandlerMethodAnnotation() throws Exception {
        Method method = TestService.class.getMethod("annotatedMethod", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestService(), method);

        TestAnnotation annotation = HandlerMethodUtil.getAnnotation(handlerMethod, TestAnnotation.class);

        assertNotNull(annotation, "应从 HandlerMethod 找到方法级别注解");
        assertTrue(annotation.value().contains("方法级别"), "应获取到方法级别的注解");
    }

    @Test
    @DisplayName("测试从 HandlerMethod 获取类级别注解（方法无注解时）")
    void testGetHandlerMethodClassAnnotation() throws Exception {
        Method method = TestService.class.getMethod("nonAnnotatedMethod", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestService(), method);

        TestAnnotation annotation = HandlerMethodUtil.getAnnotation(handlerMethod, TestAnnotation.class);

        assertNotNull(annotation, "应从 HandlerMethod 的 BeanType 找到类级别注解");
        assertTrue(annotation.value().contains("类级别"), "应获取到类级别的注解");
    }

    @Test
    @DisplayName("测试从 HandlerMethod 获取注解 - 类和方法都无注解")
    void testGetHandlerMethodNonExistentAnnotation() throws Exception {
        Method method = NoAnnotationService.class.getMethod("method");
        HandlerMethod handlerMethod = new HandlerMethod(new NoAnnotationService(), method);

        TestAnnotation annotation = HandlerMethodUtil.getAnnotation(handlerMethod, TestAnnotation.class);

        assertNull(annotation, "类和方法都无注解时应返回 null");
    }

    @Test
    @DisplayName("测试从 HandlerMethod 获取不存在的注解类型")
    void testGetHandlerMethodDifferentAnnotationType() throws Exception {
        Method method = TestService.class.getMethod("annotatedMethod", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestService(), method);

        OtherAnnotation annotation = HandlerMethodUtil.getAnnotation(handlerMethod, OtherAnnotation.class);

        assertNull(annotation, "请求不存在的注解类型时应返回 null");
    }
}
