package ext.library.servlet.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 请求体可重复读包装测试
 */
@DisplayName("请求体可重复读包装测试")
class RepeatableRequestWrapperTest {

    private static MockHttpServletRequest requestWithBody(String body) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return request;
    }

    @Test
    @DisplayName("getInputStream 可读两次，内容一致")
    void inputStreamReadableTwice() throws Exception {
        RepeatableRequestWrapper wrapper = new RepeatableRequestWrapper(requestWithBody("{\"a\":1}"));
        byte[] first = wrapper.getInputStream().readAllBytes();
        byte[] second = wrapper.getInputStream().readAllBytes();
        assertArrayEquals(first, second);
        assertEquals("{\"a\":1}", new String(second, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("getReader 与 getInputStream 各自从头读")
    void readerStartsFromBeginning() throws Exception {
        RepeatableRequestWrapper wrapper = new RepeatableRequestWrapper(requestWithBody("hello"));
        assertEquals(5, wrapper.getInputStream().readAllBytes().length);
        assertEquals("hello", wrapper.getReader().readLine());
    }

    @Test
    @DisplayName("getBody/getBodyAsString 返回缓存内容")
    void bodyAccessors() throws Exception {
        RepeatableRequestWrapper wrapper = new RepeatableRequestWrapper(requestWithBody("abc"));
        assertArrayEquals("abc".getBytes(StandardCharsets.UTF_8), wrapper.getBody());
        assertEquals("abc", wrapper.getBodyAsString());
    }

    @Test
    @DisplayName("contentLength 反映缓存长度")
    void contentLength() throws Exception {
        RepeatableRequestWrapper wrapper = new RepeatableRequestWrapper(requestWithBody("12345"));
        assertEquals(5, wrapper.getContentLength());
        assertEquals(5L, wrapper.getContentLengthLong());
    }

}
