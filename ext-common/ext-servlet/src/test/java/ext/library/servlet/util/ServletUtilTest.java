package ext.library.servlet.util;

import ext.library.servlet.response.R;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * ServletUtil 工具类测试
 */
@DisplayName("ServletUtil 工具类测试")
class ServletUtilTest {

    @AfterEach
    void clearContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void bind(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("getRequestOrNull：非 Web 线程返回 null")
    void getRequestOrNullOutsideWeb() {
        assertNull(ServletUtil.getRequestOrNull());
        assertNull(ServletUtil.getResponseOrNull());
    }

    @Test
    @DisplayName("getRequestOrNull：Web 线程返回当前请求")
    void getRequestOrNullInWeb() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        bind(request);
        assertEquals(request, ServletUtil.getRequestOrNull());
    }

    @Test
    @DisplayName("getIpAddr：多级代理取第一个有效 IP")
    void getIpAddrFromHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown, 203.0.113.9, 10.0.0.1");
        assertEquals("203.0.113.9", ServletUtil.getIpAddr(request));
    }

    @Test
    @DisplayName("getIpAddr：无代理头时回落 remoteAddr")
    void getIpAddrFallback() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.7");
        assertEquals("192.0.2.7", ServletUtil.getIpAddr(request));
    }

    @Test
    @DisplayName("getHeader：缺失头返回空串")
    void getHeaderDefaultEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals("", ServletUtil.getHeader(request, "X-Not-Exists"));
        request.addHeader("X-Trace", "abc");
        assertEquals("abc", ServletUtil.getHeader(request, "X-Trace"));
    }

    @Test
    @DisplayName("writeJson：业务码不落入 HTTP 状态")
    void writeJsonWithBizCode() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletUtil.writeJson(response, R.failed(600, "业务失败"));
        assertEquals(200, response.getStatus());
        assertEquals("{\"code\":600,\"msg\":\"业务失败\"}", response.getContentAsString());
    }

    @Test
    @DisplayName("writeJson：HTTP 状态码沿用")
    void writeJsonWithHttpStatus() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletUtil.writeJson(response, R.failed(401, "未授权"));
        assertEquals(401, response.getStatus());
    }

}
