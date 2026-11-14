package ext.library.metrics.web;

import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.properties.MetricsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DashboardServlet} 的用例
 */
class DashboardServletTest {

    private final MetricsProperties properties = new MetricsProperties();

    private final MetricsRegistry registry = new MetricsRegistry(properties);

    private final DashboardServlet servlet = new DashboardServlet(registry, properties);

    @Test
    void servesDashboardPage() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/metrics");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        String body = response.getContentAsString();
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("text/html"));
        assertTrue(body.contains("QPS"), "页面应含 QPS 面板");
        assertTrue(body.contains("/snapshot"), "页面应指向快照接口");
        assertTrue(response.getHeader("Cache-Control").contains("no-store"));
    }

    @Test
    void servesSnapshotJson() throws Exception {
        registry.http("/api", 200, 12);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/metrics/snapshot");
        request.setServletPath("/metrics");
        request.setPathInfo("/snapshot");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        String body = response.getContentAsString();
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(body.contains("\"rates\""));
        assertTrue(body.contains("\"/api\""));
    }

    @Test
    void acceptsMatchingToken() throws Exception {
        properties.getDashboard().setToken("s3cret");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/metrics");
        request.addHeader("X-Metrics-Token", "s3cret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new DashboardServlet(registry, properties).service(request, response);

        assertEquals(200, response.getStatus());
    }

    @Test
    void rejectsWrongToken() throws Exception {
        properties.getDashboard().setToken("s3cret");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/metrics");
        request.addHeader("X-Metrics-Token", "nope");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new DashboardServlet(registry, properties).service(request, response);

        assertEquals(403, response.getStatus());
    }

    @Test
    void rejectsLoopbackWhenProxyHeadersPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/metrics/snapshot");
        // 反向代理后 remoteAddr 恒为 127.0.0.1，本机不再是安全边界
        request.addHeader("X-Forwarded-For", "203.0.113.7");
        MockHttpServletResponse response = new MockHttpServletResponse();

        servlet.service(request, response);

        assertEquals(403, response.getStatus());
    }
}
