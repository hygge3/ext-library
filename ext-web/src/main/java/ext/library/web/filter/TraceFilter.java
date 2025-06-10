package ext.library.web.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ext.library.tool.$;
import ext.library.web.config.properties.WebMvcProperties;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 跟踪过滤器
 */
@RequiredArgsConstructor
@Slf4j
@AutoConfiguration
public class TraceFilter extends OncePerRequestFilter {

    final WebMvcProperties properties;

    static final ThreadLocal<String> TRACE_ID = new InheritableThreadLocal<>();

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String traceIdHeaderName = properties.getTraceIdHeaderName();
        try {
            // 生成并设置 TraceID
            String traceId = $.getObjectId();
            TRACE_ID.set(traceId);
            MDC.put(traceIdHeaderName, traceId);
            // 透传 TraceID 到下游（可选）
            response.setHeader(traceIdHeaderName, traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 必须清理上下文
            TRACE_ID.remove();
            MDC.remove(traceIdHeaderName);
        }
    }

}
