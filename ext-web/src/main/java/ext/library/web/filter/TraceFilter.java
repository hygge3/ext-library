package ext.library.web.filter;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.IDUtil;
import ext.library.web.properties.WebMvcProperties;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 跟踪过滤器
 */
@AutoConfiguration
public class TraceFilter extends OncePerRequestFilter {
    private final ThreadLocal<String> TRACE_ID = new InheritableThreadLocal<>();
    private final WebMvcProperties properties;

    public TraceFilter(WebMvcProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceIdHeaderName = properties.getTraceIdHeaderName();
        // 生成并设置 TraceID
        String traceId = IDUtil.getObjectId();
        // 在作用域中设置值
        TRACE_ID.set(traceId);
        MDC.put(traceIdHeaderName, traceId);
        response.setHeader(traceIdHeaderName, traceId);
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException e) {
            throw new ExtException(EmojiSymbol.WEB, e);
        } finally {
            MDC.remove(traceIdHeaderName);
        }
    }
}
