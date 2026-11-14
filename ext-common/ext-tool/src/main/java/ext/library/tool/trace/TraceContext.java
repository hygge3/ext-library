package ext.library.tool.trace;

import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 链路追踪上下文
 * <p>
 * 只放与协议无关的部分：生成 traceId、读写 MDC、拼出站请求头。放在 ext-tool 是因为出站 HTTP、定时任务、
 * 实时通信都要用它，而这些下层模块不能依赖 ext-trace（那个模块带 Servlet 过滤器）。
 * <p>
 * 入站解析在 ext-trace 的 {@code TraceFilter}，出站注入在 {@code HttpUtil} 与 {@code TraceAutoConfiguration}
 * 注册的 RestTemplate / RestClient 定制器，跨线程传播在 ext-core 的任务装饰器。
 *
 * @since 4.1.0
 */
public final class TraceContext {

    /** MDC 中的追踪 ID 键名 */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    /** MDC 中的请求 ID 键名 */
    public static final String REQUEST_ID_MDC_KEY = "requestId";

    /** 出站请求头：追踪 ID */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

    /** 出站请求头：W3C 追踪上下文 */
    public static final String HEADER_TRACEPARENT = "traceparent";

    /** W3C trace-id 的形状：32 位小写十六进制 */
    private static final Pattern W3C_TRACE_ID = Pattern.compile("[0-9a-f]{32}");

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private TraceContext() {}

    /**
     * 生成 32 位十六进制追踪 ID
     * <p>
     * 用 W3C trace-id 的长度而不是短 ID：这样同一个值既能进 X-Request-Id 也能直接进 traceparent，
     * 不需要为两种头各生成一个、也不需要哈希转换。
     */
    public static String generate() {
        long hi = ThreadLocalRandom.current().nextLong();
        long lo = ThreadLocalRandom.current().nextLong();
        char[] out = new char[32];
        for (int i = 15; i >= 0; i--) {
            out[i] = HEX[(int) (hi & 0xF)];
            hi >>>= 4;
            out[i + 16] = HEX[(int) (lo & 0xF)];
            lo >>>= 4;
        }
        return new String(out);
    }

    /**
     * 当前线程的追踪 ID
     *
     * @return 没有时返回 {@code null}
     */
    public static @Nullable String current() {
        return MDC.get(TRACE_ID_MDC_KEY);
    }

    /**
     * 当前线程的追踪 ID，没有就生成一个
     * <p>
     * 生成的值<b>不写入 MDC</b>：调用方多是「只借一次」的场景（出站请求、握手属性），
     * 写进去会在线程池线程上残留到下个任务。
     */
    public static String currentOrGenerate() {
        String traceId = current();
        return traceId != null && !traceId.isBlank() ? traceId : generate();
    }

    /**
     * 把追踪 ID 绑定到当前线程
     *
     * @param traceId 追踪 ID
     *
     * @return 绑定前的 MDC 快照，交给 {@link #restore(Map)} 还原
     */
    public static Map<String, String> bind(String traceId) {
        Map<String, String> snapshot = snapshot();
        MDC.put(TRACE_ID_MDC_KEY, traceId);
        MDC.put(REQUEST_ID_MDC_KEY, traceId);
        return snapshot;
    }

    /**
     * MDC 快照
     *
     * @return 空 Map 表示当前没有上下文
     */
    public static Map<String, String> snapshot() {
        Map<String, String> context = MDC.getCopyOfContextMap();
        return context == null ? new LinkedHashMap<>() : context;
    }

    /**
     * 还原 MDC
     *
     * @param snapshot {@link #bind(String)} 或 {@link #snapshot()} 的返回值
     */
    public static void restore(@Nullable Map<String, String> snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(snapshot);
        }
    }

    /**
     * 出站追踪头
     * <p>
     * 当前线程没有 traceId 时现生成一个：不写 MDC，只保证下游能拿到一个可用 ID。
     * {@code traceparent} 只在 traceId 是 W3C 形状时才写，不伪造，免得外部追踪系统收到不合规的值。
     *
     * @return 头名到值的映射，可直接写进请求
     */
    public static Map<String, String> outboundHeaders() {
        return outboundHeaders(null);
    }

    /**
     * 出站追踪头，以调用方已设的 ID 为准
     * <p>
     * 调用方显式设了 {@code X-Request-Id} 就别另生成一个：两个头指向不同 ID 时，
     * 下游拿 {@code traceparent} 建出的父子关系是错的。
     *
     * @param callerTraceId 调用方已设的追踪 ID，{@code null} 表示沿用当前线程
     *
     * @return 头名到值的映射
     */
    public static Map<String, String> outboundHeaders(@Nullable String callerTraceId) {
        if (!enabled) {
            return Map.of();
        }
        String traceId = callerTraceId != null && !callerTraceId.isBlank() ? callerTraceId : currentOrGenerate();
        Map<String, String> headers = new LinkedHashMap<>(2);
        headers.put(HEADER_REQUEST_ID, traceId);
        if (W3C_TRACE_ID.matcher(traceId).matches()) {
            headers.put(HEADER_TRACEPARENT, "00-" + traceId + "-" + spanId() + "-01");
        }
        return headers;
    }

    /**
     * 用一条新链路包住任务
     * <p>
     * 给没有入站请求的入口用：{@code @Scheduled} 任务、MQ 消费端。任务内部发的出站请求会带上这个 traceId。
     *
     * @param task 任务
     *
     * @return 已绑定 traceId 的任务
     */
    public static Runnable wrap(Runnable task) {
        String traceId = generate();
        return () -> {
            Map<String, String> snapshot = bind(traceId);
            try {
                task.run();
            } finally {
                restore(snapshot);
            }
        };
    }

    /**
     * 追踪总开关，由 ext-trace 的自动配置按 {@code ext.trace.enabled} 设置
     * <p>
     * 关掉时 {@link #outboundHeaders} 不再注入头：消费方换成 OpenTelemetry 之类的方案时，
     * 两套 {@code traceparent} 会互相覆盖。
     */
    private static volatile boolean enabled = true;

    /**
     * 设置追踪开关
     *
     * @param value 是否启用追踪头的注入
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    private static String spanId() {
        long v = ThreadLocalRandom.current().nextLong();
        char[] out = new char[16];
        for (int i = 15; i >= 0; i--) {
            out[i] = HEX[(int) (v & 0xF)];
            v >>>= 4;
        }
        return new String(out);
    }

}
