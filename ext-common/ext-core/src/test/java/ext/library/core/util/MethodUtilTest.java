package ext.library.core.util;

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
 * MethodUtil 工具类测试
 */
@DisplayName("MethodUtil 工具类测试")
class MethodUtilTest {

    private final MethodUtil methodUtil = new MethodUtil();

    // ========== 测试注解 ==========

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        String value() default "";
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface ComposedAnnotation {
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

        @ComposedAnnotation
        public void composedAnnotatedMethod() {
        }

        public void plainMethod() {
        }
    }

    // ========== getMethodParameter(Constructor, int) 测试 ==========

    @Test
    @DisplayName("测试获取构造器参数信息")
    void testGetConstructorParameter() throws Exception {
        // 使用 String 的构造器进行测试
        var constructor = String.class.getDeclaredConstructor(byte[].class);

        var parameter = methodUtil.getMethodParameter(constructor, 0);

        assertNotNull(parameter, "参数信息不应为 null");
    }

    // ========== getMethodParameter(Method, int) 测试 ==========

    @Test
    @DisplayName("测试获取方法参数信息")
    void testGetMethodParameter() throws Exception {
        Method method = TestService.class.getMethod("annotatedMethod", String.class);

        var parameter = methodUtil.getMethodParameter(method, 0);

        assertNotNull(parameter, "参数信息不应为 null");
    }

    @Test
    @DisplayName("测试获取多参数方法参数信息")
    void testGetMultiParameterMethod() throws Exception {
        class MultiParamService {
            public String multiParamMethod(String p1, Integer p2, Boolean p3) {
                return p1 + p2 + p3;
            }
        }

        Method method = MultiParamService.class.getMethod("multiParamMethod", String.class, Integer.class, Boolean.class);

        for (int i = 0; i < 3; i++) {
            var parameter = methodUtil.getMethodParameter(method, i);
            assertNotNull(parameter, "参数 " + i + " 信息不应为 null");
        }
    }

    // ========== getAnnotation(Method, Class) 测试 ==========

    @Test
    @DisplayName("测试获取方法上的注解")
    void testGetMethodAnnotation() throws Exception {
        Method method = TestService.class.getMethod("annotatedMethod", String.class);

        TestAnnotation annotation = methodUtil.getAnnotation(method, TestAnnotation.class);

        assertNotNull(annotation, "应找到方法上的注解");
        assertTrue(annotation.value().contains("方法级别"), "应获取到方法级别的注解");
    }

    @Test
    @DisplayName("测试获取类上的注解（方法无注解时）")
    void testGetClassAnnotationWhenMethodNone() throws Exception {
        Method method = TestService.class.getMethod("nonAnnotatedMethod", String.class);

        TestAnnotation annotation = methodUtil.getAnnotation(method, TestAnnotation.class);

        assertNotNull(annotation, "应找到类上的注解");
        assertTrue(annotation.value().contains("类级别"), "应获取到类级别的注解");
    }

    @Test
    @DisplayName("测试获取不存在的注解")
    void testGetNonExistentAnnotation() throws Exception {
        // 由于 TestService 类上有 @TestAnnotation 注解，
        // 即使方法 plainMethod 没有注解，也会返回类级别的注解
        Method method = TestService.class.getMethod("plainMethod");

        TestAnnotation annotation = methodUtil.getAnnotation(method, TestAnnotation.class);

        // 会找到类级别的注解作为回退
        assertNotNull(annotation, "会返回类级别的注解作为回退");
    }

    @Test
    @DisplayName("测试获取组合注解")
    void testGetComposedAnnotation() throws Exception {
        Method method = TestService.class.getMethod("composedAnnotatedMethod");

        TestAnnotation annotation = methodUtil.getAnnotation(method, TestAnnotation.class);

        // 方法上有 @ComposedAnnotation，但由于类上有 @TestAnnotation，会返回类级别注解
        assertNotNull(annotation, "会返回类级别的注解作为回退");
    }

    // ========== getAnnotation(HandlerMethod, Class) 测试 ==========

    @Test
    @DisplayName("测试从 HandlerMethod 获取注解")
    void testGetHandlerMethodAnnotation() throws Exception {
        Method method = TestService.class.getMethod("annotatedMethod", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestService(), method);

        TestAnnotation annotation = methodUtil.getAnnotation(handlerMethod, TestAnnotation.class);

        assertNotNull(annotation, "应从 HandlerMethod 找到注解");
    }

    @Test
    @DisplayName("测试从 HandlerMethod 获取类级别注解")
    void testGetHandlerMethodClassAnnotation() throws Exception {
        Method method = TestService.class.getMethod("nonAnnotatedMethod", String.class);
        HandlerMethod handlerMethod = new HandlerMethod(new TestService(), method);

        TestAnnotation annotation = methodUtil.getAnnotation(handlerMethod, TestAnnotation.class);

        assertNotNull(annotation, "应从 HandlerMethod 的 BeanType 找到类级别注解");
    }

    @Test
    @DisplayName("测试从 HandlerMethod 获取不存在的注解")
    void testGetHandlerMethodNonExistentAnnotation() throws Exception {
        Method method = TestService.class.getMethod("plainMethod");
        HandlerMethod handlerMethod = new HandlerMethod(new TestService(), method);

        TestAnnotation annotation = methodUtil.getAnnotation(handlerMethod, TestAnnotation.class);

        // 会找到类级别的注解作为回退
        assertNotNull(annotation, "会返回类级别的注解作为回退");
    }

    // ========== 边界情况测试 ==========

    @Test
    @DisplayName("测试处理接口方法")
    void testInterfaceMethod() throws Exception {
        interface TestInterface {
            @TestAnnotation("接口方法注解")
            String interfaceMethod(String param);
        }

        class TestImpl implements TestInterface {
            @Override
            public String interfaceMethod(String param) {
                return param;
            }
        }

        Method method = TestInterface.class.getMethod("interfaceMethod", String.class);

        TestAnnotation annotation = methodUtil.getAnnotation(method, TestAnnotation.class);

        assertNotNull(annotation, "应找到接口方法上的注解");
        assertTrue(annotation.value().contains("接口方法"), "应获取到接口方法的注解");
    }
}
