package ext.library.web.filter;

import ext.library.tool.core.Exceptions;
import ext.library.tool.util.IDUtil;
import ext.library.web.config.properties.WebMvcProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.annotation.Nonnull;
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
    private final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final WebMvcProperties properties;

    public TraceFilter(WebMvcProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String traceIdHeaderName = properties.getTraceIdHeaderName();
        // 生成并设置 TraceID
        // 在作用域中设置值
        ScopedValue.where(TRACE_ID, IDUtil.getObjectId()).run(() -> {
            MDC.put(traceIdHeaderName, TRACE_ID.get());
            response.setHeader(traceIdHeaderName, TRACE_ID.get());
            try {
                filterChain.doFilter(request, response);
            } catch (IOException | ServletException e) {
                throw Exceptions.unchecked(e);
            } finally {
                MDC.remove(traceIdHeaderName);
            }
        });
    }

}