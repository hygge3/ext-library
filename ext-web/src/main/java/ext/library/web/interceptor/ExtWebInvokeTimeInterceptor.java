package ext.library.web.interceptor;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ext.library.json.util.JsonUtil;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * web 的调用时间统计拦截器
 */
@Slf4j
@RequiredArgsConstructor
public class ExtWebInvokeTimeInterceptor implements HandlerInterceptor {

    private final static ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler) throws Exception {

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = Symbol.EMPTY;
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            // 处理请求数据
            byte[] body = requestWrapper.getContentAsByteArray();
            if ($.isNotEmpty(body)) {
                jsonParam = new String(body);
            }
            log.info("[🌐] => {}:{},[body],[{}]", request.getMethod(), request.getRequestURI(), jsonParam);
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if ($.isNotEmpty(parameterMap)) {
                String parameters = JsonUtil.toJson(parameterMap);
                log.info("[🌐] => {}:{},[query],[{}]", request.getMethod(), request.getRequestURI(), parameters);
            } else {
                log.info("[🌐] => {}:{}", request.getMethod(), request.getRequestURI());
            }
        }

        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) throws Exception {
        StopWatch stopWatch = KEY_CACHE.get();
        stopWatch.stop();
        log.info("[🌐] => {}:{},take:[{}]ms", request.getMethod(), request.getRequestURI(), stopWatch.getTotalTimeMillis());
        KEY_CACHE.remove();
    }

    /**
     * 判断本次请求的数据类型是否为 json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJsonRequest(@NotNull HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            return contentType.startsWith(MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }

}
