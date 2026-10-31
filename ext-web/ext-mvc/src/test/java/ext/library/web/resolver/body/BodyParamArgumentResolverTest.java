package ext.library.web.resolver.body;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 之前用 {@code JsonNodeUtil.toNode(request.getReader())} —— 那是值序列化，
 * 把 Reader 当普通对象序列化，结果恒为 {@code {}}，注解永远读不到请求体。
 */
@DisplayName("BodyParam 参数解析")
class BodyParamArgumentResolverTest {

    private final BodyParamArgumentResolver resolver = new BodyParamArgumentResolver();

    public void handle(@BodyParam("name") String name) {
    }

    @Test
    @DisplayName("从请求体读出字段值")
    void readsFieldFromBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("{\"name\":\"张三\"}".getBytes(StandardCharsets.UTF_8));

        Object value = resolver.resolveArgument(parameter(), null, webRequest(request), null);

        assertThat(value).isEqualTo("张三");
    }

    @Test
    @DisplayName("空请求体按 required 报缺失参数")
    void emptyBodyThrowsForRequired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType("application/json");
        request.setContent("{}".getBytes(StandardCharsets.UTF_8));

        NativeWebRequest webRequest = webRequest(request);
        MethodParameter parameter = parameter();
        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, webRequest, null))
                .hasMessageContaining("name");
    }

    private MethodParameter parameter() throws Exception {
        Method method = getClass().getMethod("handle", String.class);
        return new MethodParameter(method, 0);
    }

    private NativeWebRequest webRequest(MockHttpServletRequest request) {
        return new ServletWebRequest(request);
    }

}
