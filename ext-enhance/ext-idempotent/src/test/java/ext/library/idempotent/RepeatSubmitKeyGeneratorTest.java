package ext.library.idempotent;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 键必须能区分「谁、哪个接口、什么参数」，且不能把参数明文写进缓存
 */
@DisplayName("防重提交键生成器")
class RepeatSubmitKeyGeneratorTest {

    private final RepeatSubmitKeyGenerator generator = new RepeatSubmitKeyGenerator();

    static class Service {

        @RepeatSubmit
        public void create(String phone) {
        }

        @RepeatSubmit(prefix = "other", uniqueExpression = "#p0")
        public void withExpression(String orderNo) {
        }
    }

    private void givenRequest(String user, String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setUserPrincipal(() -> user);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void clear() {
        RequestContextHolder.resetRequestAttributes();
    }

    private String key(String methodName, String arg, RepeatSubmit annotation) throws Exception {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(Service.class.getMethod(methodName, String.class));
        JoinPoint point = mock(JoinPoint.class);
        when(point.getSignature()).thenReturn(signature);
        when(point.getTarget()).thenReturn(new Service());
        when(point.getArgs()).thenReturn(new Object[]{arg});
        return generator.generate(point, annotation);
    }

    private RepeatSubmit annotation(String methodName) throws Exception {
        return Service.class.getMethod(methodName, String.class).getAnnotation(RepeatSubmit.class);
    }

    @Test
    @DisplayName("同一用户、同一接口、同一参数生成同一个键")
    void sameRequestSameKey() throws Exception {
        givenRequest("u1", "POST", "/api/order");

        assertThat(key("create", "13800000000", annotation("create")))
                .isEqualTo(key("create", "13800000000", annotation("create")));
    }

    @Test
    @DisplayName("参数不同则键不同")
    void differentArgsDifferentKey() throws Exception {
        givenRequest("u1", "POST", "/api/order");

        assertThat(key("create", "13800000000", annotation("create")))
                .isNotEqualTo(key("create", "13900000000", annotation("create")));
    }

    @Test
    @DisplayName("用户不同则键不同")
    void differentUserDifferentKey() throws Exception {
        givenRequest("u1", "POST", "/api/order");
        String first = key("create", "13800000000", annotation("create"));

        givenRequest("u2", "POST", "/api/order");

        assertThat(first).isNotEqualTo(key("create", "13800000000", annotation("create")));
    }

    @Test
    @DisplayName("接口不同则键不同")
    void differentUriDifferentKey() throws Exception {
        givenRequest("u1", "POST", "/api/order");
        String first = key("create", "13800000000", annotation("create"));

        givenRequest("u1", "POST", "/api/refund");

        assertThat(first).isNotEqualTo(key("create", "13800000000", annotation("create")));
    }

    @Test
    @DisplayName("键里不留参数明文")
    void noPlaintextInKey() throws Exception {
        givenRequest("u1", "POST", "/api/order");

        assertThat(key("create", "13800000000", annotation("create"))).doesNotContain("13800000000").doesNotContain("u1");
    }

    @Test
    @DisplayName("prefix 与表达式参与键")
    void prefixAndExpression() throws Exception {
        givenRequest("u1", "POST", "/api/order");

        assertThat(key("withExpression", "A001", annotation("withExpression"))).startsWith("other:");
    }

    @Test
    @DisplayName("无 Servlet 上下文时退回类名#方法名")
    void withoutRequest() throws Exception {
        assertThat(key("create", "13800000000", annotation("create"))).contains("Service#create");
    }
}
