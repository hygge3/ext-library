package ext.library.web.interceptor;

import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.Symbol;
import ext.library.tool.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * web 的调用时间统计拦截器
 */
public class ExtWebInvokeTimeInterceptor implements HandlerInterceptor {
    private final static ThreadLocal<StopWatch> KEY_CACHE = new InheritableThreadLocal<>();
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) throws Exception {

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = Symbol.EMPTY;
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            // 处理请求数据
            byte[] body = requestWrapper.getContentAsByteArray();
            if (ObjectUtil.isNotEmpty(body)) {
                jsonParam = new String(body);
            }
            log.info("[🌐] {}:{},body:[{}]", request.getMethod(), request.getRequestURI(), jsonParam);
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (ObjectUtil.isNotEmpty(parameterMap)) {
                String parameters = JsonUtil.toJson(parameterMap);
                log.info("[🌐] {}:{},query:[{}]", request.getMethod(), request.getRequestURI(), parameters);
            } else {
                log.info("[🌐] {}:{}", request.getMethod(), request.getRequestURI());
            }
        }

        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
        return true;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                @Nonnull Object handler, Exception ex) throws Exception {
        StopWatch stopWatch = KEY_CACHE.get();
        stopWatch.stop();
        log.info("[🌐] {}:{},take:[{}]ms", request.getMethod(), request.getRequestURI(), stopWatch.getTotalTimeMillis());
        KEY_CACHE.remove();
    }

    /**
     * 判断本次请求的数据类型是否为 json
     *
     * @param request request
     *
     * @return boolean
     */
    private boolean isJsonRequest(@Nonnull HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            return contentType.startsWith(MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }

}