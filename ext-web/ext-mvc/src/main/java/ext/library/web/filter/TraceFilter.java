package ext.library.web.filter;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.IdUtil;
import ext.library.web.properties.WebMvcProperties;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跟踪过滤器
 * <p>
 * 使用 {@link ScopedValue} 在请求作用域内绑定 Trace ID，自动随作用域结束清理。
 */
public class TraceFilter extends OncePerRequestFilter {

    private static final ScopedValue<String> traceId = ScopedValue.newInstance();
    private final WebMvcProperties properties;

    public TraceFilter(WebMvcProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取当前请求的 Trace ID
     *
     * @return Trace ID，未在请求上下文中时返回 null
     */
    public static String getTraceId() {
        return traceId.orElse(null);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceIdHeaderName = properties.getTraceIdHeaderName();
        String id = IdUtil.getObjectId();
        MDC.put(traceIdHeaderName, id);
        response.setHeader(traceIdHeaderName, id);
        try {
            ScopedValue.where(traceId, id).call(() -> {
                filterChain.doFilter(request, response);
                return null;
            });
        } catch (Exception e) {
            throw new ExtException(EmojiSymbol.WEB, e);
        } finally {
            MDC.remove(traceIdHeaderName);
        }
    }
}
