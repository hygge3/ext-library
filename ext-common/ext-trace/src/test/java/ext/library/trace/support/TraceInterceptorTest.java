package ext.library.trace.support;

import ext.library.tool.trace.TraceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link TraceInterceptor} 的用例
 */
class TraceInterceptorTest {

    private static final String TRACE_ID = "0123456789abcdef0123456789abcdef";

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void writesTraceHeadersFromMdc() throws Exception {
        Map<String, String> snapshot = TraceContext.bind(TRACE_ID);
        try {
            MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/api"));

            new TraceInterceptor().intercept(request, new byte[0], ok());

            assertEquals(TRACE_ID, request.getHeaders().getFirst(TraceContext.HEADER_REQUEST_ID));
            assertEquals("00-" + TRACE_ID + "-", request.getHeaders().getFirst(TraceContext.HEADER_TRACEPARENT)
                    .substring(0, 3 + TRACE_ID.length() + 1));
        } finally {
            TraceContext.restore(snapshot);
        }
    }

    @Test
    void keepsCallerSuppliedHeader() throws Exception {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/api"));
        request.getHeaders().set(TraceContext.HEADER_REQUEST_ID, "upstream-id");

        new TraceInterceptor().intercept(request, new byte[0], ok());

        assertEquals("upstream-id", request.getHeaders().getFirst(TraceContext.HEADER_REQUEST_ID));
        assertNull(request.getHeaders().getFirst(TraceContext.HEADER_TRACEPARENT), "非 W3C 形状的 traceId 不写 traceparent");
    }

    private static ClientHttpRequestExecution ok() {
        return (request, body) -> new MockClientHttpResponse(new byte[0], HttpStatus.OK);
    }

}
