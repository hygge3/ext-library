package ext.library.web.interceptor;

import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * web 的调用时间统计拦截器
 */
public class ExtWebInvokeTimeInterceptor implements HandlerInterceptor {
    private final static ThreadLocal<StopWatch> KEY_CACHE = new InheritableThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = "";
            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 0);
            // 处理请求数据
            byte[] body = requestWrapper.getContentAsByteArray();
            if (ObjectUtil.isNotEmpty(body)) {
                jsonParam = new String(body);
            }
            Logs.info(EmojiSymbol.WEB, "{}:{},body:[{}]", request.getMethod(), request.getRequestURI(), jsonParam);
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (ObjectUtil.isNotEmpty(parameterMap)) {
                String parameters = JsonUtil.toJson(parameterMap);
                Logs.info(EmojiSymbol.WEB, "{}:{},query:[{}]", request.getMethod(), request.getRequestURI(), parameters);
            } else {
                Logs.info(EmojiSymbol.WEB, "{}:{}", request.getMethod(), request.getRequestURI());
            }
        }

        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) throws Exception {
        StopWatch stopWatch = KEY_CACHE.get();
        stopWatch.stop();
        Logs.info(EmojiSymbol.WEB, "{}:{},take:[{}]ms", request.getMethod(), request.getRequestURI(), stopWatch.getTotalTimeMillis());
        KEY_CACHE.remove();
    }

    /**
     * 判断本次请求的数据类型是否为 json
     *
     * @param request request
     *
     * @return boolean
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            return contentType.startsWith(MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }

}
