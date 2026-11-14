package ext.library.accesslog.filter;

import ext.library.accesslog.properties.AccessLogProperties;
import ext.library.servlet.util.ServletUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.StringUtil;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 结构化访问日志过滤器
 */
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie", "x-api-key", "proxy-authorization"
    );

    /**
     * 查询串里需要掩码的参数名
     * <p>
     * 请求头有白名单，URL 没有：{@code ?access_token=...} 会跟着访问日志一起落盘。
     */
    private static final Set<String> SENSITIVE_PARAMS = Set.of(
            "token", "access_token", "refresh_token", "id_token", "password", "pwd",
            "secret", "sign", "signature", "id_card", "idcard", "phone", "mobile"
    );

    private final AccessLogProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccessLogFilter(AccessLogProperties properties) {
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
        String query = properties.isIncludeQueryString() ? maskQuery(request.getQueryString()) : null;
        String path = StringUtil.isNotBlank(query) ? uri + "?" + query : uri;
        int status = response.getStatus();
        String traceId = firstNonBlank(MDC.get(properties.getTraceIdMdcKey()),
                MDC.get(properties.getRequestIdMdcKey()));

        StringBuilder msg = new StringBuilder(256);
        msg.append("HTTP ").append(method).append(' ').append(path)
                .append(" | status=").append(status)
                .append(" | duration=").append(duration).append("ms");
        if (StringUtil.isNotBlank(traceId)) {
            msg.append(" | traceId=").append(traceId);
        }
        if (properties.isIncludeClientIp()) {
            msg.append(" | ip=").append(ServletUtil.getIpAddr(request));
        }
        if (properties.isIncludeUserAgent()) {
            String ua = request.getHeader(HttpHeaders.USER_AGENT);
            if (StringUtil.isNotBlank(ua)) {
                msg.append(" | ua=").append(abbreviate(ua, 160));
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

    private static String abbreviate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...";
    }

    static String maskQuery(String query) {
        if (StringUtil.isBlank(query)) {
            return query;
        }
        StringBuilder masked = new StringBuilder(query.length());
        for (String pair : query.split("&")) {
            if (!masked.isEmpty()) {
                masked.append('&');
            }
            int eq = pair.indexOf('=');
            if (eq > 0 && SENSITIVE_PARAMS.contains(pair.substring(0, eq).toLowerCase(Locale.ROOT))) {
                masked.append(pair, 0, eq + 1).append("***");
            } else {
                masked.append(pair);
            }
        }
        return masked.toString();
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtil.isNotBlank(a)) return a;
        if (StringUtil.isNotBlank(b)) return b;
        return null;
    }
}
