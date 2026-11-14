package ext.library.trace.filter;

import ext.library.servlet.util.ServletUtil;
import ext.library.trace.properties.TraceProperties;
import ext.library.tool.trace.TraceContext;
import ext.library.tool.util.StringUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 链路追踪过滤器：提取/生成 traceId，写入 MDC 与响应头
 */
public class TraceFilter extends OncePerRequestFilter {

    private final TraceProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 上游 traceId 白名单：外部输入会进每条日志和响应头，不限长度与字符就能被拿来膨胀日志或注入假日志行
     */
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    public TraceFilter(TraceProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String path = request.getRequestURI();
        List<String> patterns = properties.getExcludePatterns();
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (StringUtil.isBlank(pattern)) {
                continue;
            }
            if (path.startsWith(pattern) || pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 异步分发时重新进过滤器
     * <p>
     * 默认是跳过：SSE / DeferredResult 的 traceId 会在请求线程里被删掉，异步阶段日志全部丢链路。
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        String traceKey = properties.getTraceIdMdcKey();
        String requestIdKey = properties.getRequestIdMdcKey();

        String previousTrace = MDC.get(traceKey);
        String previousRequestId = requestIdKey != null ? MDC.get(requestIdKey) : null;
        try {
            MDC.put(traceKey, traceId);
            if (StringUtil.isNotBlank(requestIdKey)) {
                MDC.put(requestIdKey, traceId);
            }
            if (properties.isResponseHeaderEnabled() && StringUtil.isNotBlank(properties.getResponseHeaderName())) {
                response.setHeader(properties.getResponseHeaderName(), traceId);
            }
            filterChain.doFilter(request, response);
        } finally {
            restoreMdc(traceKey, previousTrace);
            if (StringUtil.isNotBlank(requestIdKey)) {
                restoreMdc(requestIdKey, previousRequestId);
            }
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        List<String> headers = properties.getRequestIdHeaders();
        if (headers != null) {
            for (String header : headers) {
                if (StringUtil.isBlank(header)) {
                    continue;
                }
                String value = ServletUtil.getHeader(request, header);
                if (StringUtil.isNotBlank(value)) {
                    // traceparent: 00-{traceId}-{spanId}-{flags}，取 traceId 部分
                    if ("traceparent".equalsIgnoreCase(header) && value.length() >= 55 && value.indexOf('-') > 0) {
                        String[] parts = value.split("-");
                        if (parts.length >= 2 && StringUtil.isNotBlank(parts[1])) {
                            value = parts[1];
                        }
                    }
                    // 上游脏值直接丢弃自己生成，不把外部字符串原样写进 MDC 与响应头
                    if (TRACE_ID_PATTERN.matcher(value.trim()).matches()) {
                        return value.trim();
                    }
                }
            }
        }
        return TraceContext.generate();
    }

    private static void restoreMdc(String key, String previous) {
        if (StringUtil.isBlank(key)) {
            return;
        }
        if (previous == null) {
            MDC.remove(key);
        } else {
            MDC.put(key, previous);
        }
    }
}
