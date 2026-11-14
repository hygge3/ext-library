package ext.library.metrics.web;

import ext.library.metrics.MetricsRecorder;
import ext.library.metrics.properties.MetricsProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * HTTP 指标过滤器
 * <p>
 * 端点名优先取 Spring MVC 的路径模板（{@code /user/{id}}），拿不到才退化成原始 URI 并抹掉数字段。
 * 用原始 URI 计数会让每个用户一个 key，几百个请求就把端点表撑满。
 */
public class MetricsFilter implements Filter {

    /**
     * {@code HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE} 的常量值
     * <p>
     * 不引 spring-webmvc 依赖，直接按属性名取：值在 Spring 各版本里是稳定的全限定名。
     */
    private static final String BEST_MATCHING_PATTERN = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

    private static final Pattern NUMERIC_SEGMENT = Pattern.compile("/[0-9]+(?=/|$)");

    private static final Pattern UUID_SEGMENT = Pattern.compile("/[0-9a-fA-F]{8}-[0-9a-fA-F-]{27}(?=/|$)");

    /**
     * 请求处理标记：异步派发时同一个 request 会再进一次过滤器
     */
    private static final String STARTED = MetricsFilter.class.getName() + ".started";

    private final MetricsRecorder recorder;

    private final List<String> excludePatterns;

    public MetricsFilter(MetricsRecorder recorder, MetricsProperties properties) {
        this.recorder = recorder;
        this.excludePatterns = List.of(properties.getExcludePatterns());
    }

    @Override
    public void doFilter(@NonNull ServletRequest request, @NonNull ServletResponse response, @NonNull FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)
                || excluded(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        // SSE 这类长连接在初始请求结束后还有二次派发，两次都统会把请求数和耗时长虚
        if (httpRequest.getAttribute(STARTED) != null) {
            chain.doFilter(request, response);
            return;
        }
        httpRequest.setAttribute(STARTED, Boolean.TRUE);
        long start = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000;
            recorder.http(endpoint(httpRequest), httpResponse.getStatus(), millis);
        }
    }

    /**
     * 异步派发不重复统计
     */
    private String endpoint(HttpServletRequest request) {
        Object pattern = request.getAttribute(BEST_MATCHING_PATTERN);
        if (pattern instanceof String text && !text.isBlank()) {
            return text;
        }
        return normalize(request.getRequestURI());
    }

    private static String normalize(String uri) {
        return NUMERIC_SEGMENT.matcher(UUID_SEGMENT.matcher(uri).replaceAll("/{id}")).replaceAll("/{id}");
    }

    private boolean excluded(String uri) {
        for (String pattern : excludePatterns) {
            if (!pattern.isBlank() && uri.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}
