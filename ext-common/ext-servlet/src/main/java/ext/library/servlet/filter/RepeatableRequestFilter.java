package ext.library.servlet.filter;

import ext.library.servlet.util.RepeatableRequestWrapper;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求体缓存过滤器
 * <p>
 * 把请求包装成 {@link RepeatableRequestWrapper}，使下游多个组件都能读到完整的 body。
 * 由 {@code ext.servlet.repeatable-request.enabled=true} 开启。
 *
 * @since 4.1.0
 */
public class RepeatableRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!isBodyReadable(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        filterChain.doFilter(new RepeatableRequestWrapper(request), response);
    }

    /**
     * 是否是需要缓存的请求：只有带 body 的请求才值得包装
     *
     * @param request 请求
     *
     * @return 需要缓存返回 {@code true}
     */
    private boolean isBodyReadable(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        // 表单与文件上传的 body 由容器以参数形式解析，包装反而会打断其解析
        String lowerCase = contentType.toLowerCase();
        return lowerCase.contains("json") || lowerCase.contains("text");
    }

}
