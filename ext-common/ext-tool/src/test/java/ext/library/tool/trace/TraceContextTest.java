package ext.library.tool.trace;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TraceContext} 的用例
 */
class TraceContextTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
        TraceContext.setEnabled(true);
    }

    @Test
    void generateIsW3cTraceId() {
        Set<String> ids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String id = TraceContext.generate();
            assertEquals(32, id.length());
            assertTrue(id.matches("[0-9a-f]{32}"));
            ids.add(id);
        }
        assertEquals(100, ids.size(), "生成值不应重复");
    }

    @Test
    void outboundHeadersCarryCurrentTraceId() {
        Map<String, String> snapshot = TraceContext.bind("0123456789abcdef0123456789abcdef");
        try {
            Map<String, String> headers = TraceContext.outboundHeaders();
            assertEquals("0123456789abcdef0123456789abcdef", headers.get(TraceContext.HEADER_REQUEST_ID));
            String traceparent = headers.get(TraceContext.HEADER_TRACEPARENT);
            assertTrue(traceparent.matches("00-0123456789abcdef0123456789abcdef-[0-9a-f]{16}-01"));
        } finally {
            TraceContext.restore(snapshot);
        }
    }

    @Test
    void outboundHeadersGenerateWithoutTouchingMdc() {
        Map<String, String> headers = TraceContext.outboundHeaders();

        assertEquals(32, headers.get(TraceContext.HEADER_REQUEST_ID).length());
        assertTrue(headers.containsKey(TraceContext.HEADER_TRACEPARENT));
        assertNull(TraceContext.current(), "没有上下文时不应往 MDC 里写");
    }

    @Test
    void nonW3cTraceIdSkipsTraceparent() {
        Map<String, String> snapshot = TraceContext.bind("upstream-id-1");
        try {
            Map<String, String> headers = TraceContext.outboundHeaders();

            assertEquals("upstream-id-1", headers.get(TraceContext.HEADER_REQUEST_ID));
            assertFalse(headers.containsKey(TraceContext.HEADER_TRACEPARENT), "不合规的 traceId 不伪造 traceparent");
        } finally {
            TraceContext.restore(snapshot);
        }
    }

    @Test
    void disabledSkipsOutboundHeaders() {
        TraceContext.setEnabled(false);
        try {
            assertTrue(TraceContext.outboundHeaders().isEmpty(), "关掉追踪后不再注入任何出站头");
            assertTrue(TraceContext.outboundHeaders("upstream-id-1").isEmpty(), "调用方设了 ID 也不注入");
        } finally {
            TraceContext.setEnabled(true);
        }
        assertEquals(2, TraceContext.outboundHeaders(TraceContext.generate()).size());
    }

    @Test
    void wrapBindsFreshTraceIdAndRestores() {
        String[] seen = new String[1];

        TraceContext.wrap(() -> seen[0] = TraceContext.current()).run();

        assertEquals(32, seen[0].length());
        assertNull(TraceContext.current(), "任务结束后应还原");
    }

}
