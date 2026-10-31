package ext.library.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

/**
 * 缓存 JSON 请求体，供调用时间拦截器在请求结束后打印
 * <p>
 * 拦截器自己 new 一个包装器是拿不到内容的：包装器必须由 Filter 交给下游，handler 读过的字节才会被记录。
 *
 * @since 4.1.0
 */
public class ContentCachingFilter extends OncePerRequestFilter {

    /**
     * 缓存上限（字节）：超过的部分不缓存，避免大请求占内存
     */
    private static final int CACHE_LIMIT = 4096;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (isJsonRequest(request)) {
            filterChain.doFilter(new ContentCachingRequestWrapper(request, CACHE_LIMIT), response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 判断本次请求是否为 JSON 请求
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON_VALUE);
    }

}
