package ext.library.accesslog.filter;

import ext.library.accesslog.properties.AccessLogProperties;
import ext.library.servlet.util.ServletUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
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
        long start = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            logAccess(request, response, duration);
        }
    }

    private void logAccess(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = properties.isIncludeQueryString() ? request.getQueryString() : null;
        String path = StringUtil.isNotBlank(query) ? uri + "?" + query : uri;
        int status = response.getStatus();
        String traceId = firstNonBlank(MDC.get("traceId"), MDC.get("requestId"));

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
            String ua = ServletUtil.getUA(request);
            if (StringUtil.isNotBlank(ua)) {
                msg.append(" | ua=").append(abbreviate(ua, 160));
            }
        }
        if (properties.isIncludeHeaders()) {
            msg.append(" | headers=").append(safeHeaders(request));
        }

        boolean slow = properties.getSlowThresholdMs() > 0 && duration >= properties.getSlowThresholdMs();
        if (slow) {
            Logs.warn(EmojiSymbol.WEB, "慢请求 " + msg);
        } else if (status >= 500) {
            Logs.error(EmojiSymbol.WEB, msg.toString());
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

    private static String firstNonBlank(String a, String b) {
        if (StringUtil.isNotBlank(a)) return a;
        if (StringUtil.isNotBlank(b)) return b;
        return null;
    }
}
