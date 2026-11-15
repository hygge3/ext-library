package ext.library.accesslog.filter;

import ext.library.accesslog.properties.AccessLogProperties;
import ext.library.servlet.util.ServletUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.StringUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 结构化访问日志过滤器
 */
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie", "x-api-key", "proxy-authorization"
    );

    private final AccessLogProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccessLogFilter(AccessLogProperties properties) {
        this.properties = properties;
    }

    private static String abbreviate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
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

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 纳秒单调递增，currentTimeMillis 遇到 NTP 校时回拨会算出负耗时
        long start = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 异步请求在请求线程返回时还没算完：此时记录的是「提交任务耗时」，真正的耗时要等异步分发
            if (!request.isAsyncStarted()) {
                logAccess(request, response, (System.nanoTime() - start) / 1_000_000);
            }
        }
    }

    /**
     * 异步分发时重新进过滤器
     * <p>
     * 默认跳过的话，SSE / DeferredResult 的耗时与 traceId 全部丢失；配合上面的
     * {@code isAsyncStarted()} 判断，只在最后一次分发记录一条。
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = properties.isIncludeQueryString() ? request.getQueryString() : null;
        String path = StringUtil.isNotBlank(query) ? uri + "?" + query : uri;
        int status = response.getStatus();

        StringBuilder msg = new StringBuilder(256);
        msg.append("HTTP ").append(method).append(' ').append(path)
                .append(" | ").append(status)
                .append(" | ").append(duration).append("ms");
        if (properties.isIncludeClientIp()) {
            msg.append(" | ").append(ServletUtil.getIpAddr(request));
        }
        if (properties.isIncludeUserAgent()) {
            String ua = request.getHeader(HttpHeaders.USER_AGENT);
            if (StringUtil.isNotBlank(ua)) {
                msg.append(" | ").append(abbreviate(ua, 160));
            }
        }
        if (properties.isIncludeHeaders()) {
            msg.append(" | headers=").append(safeHeaders(request));
        }

        // 500 先判：慢的 5xx 记成 WARN 会掩盖真正的错误
        boolean slow = properties.getSlowThresholdMs() > 0 && duration >= properties.getSlowThresholdMs();
        if (status >= 500) {
            Logs.error(EmojiSymbol.WEB, msg.toString());
        } else if (slow) {
            Logs.warn(EmojiSymbol.WEB, "慢请求 " + msg);
        } else if (status >= 400) {
            Logs.warn(EmojiSymbol.WEB, msg.toString());
        } else {
            Logs.info(EmojiSymbol.WEB, msg.toString());
        }
    }

    private Map<String, String> safeHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        if (names == null) {
            return map;
        }
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name == null) {
                continue;
            }
            String lower = name.toLowerCase();
            if (SENSITIVE_HEADERS.contains(lower)) {
                map.put(name, "***");
            } else {
                map.put(name, abbreviate(request.getHeader(name), 120));
            }
        }
        return map;
    }
}
