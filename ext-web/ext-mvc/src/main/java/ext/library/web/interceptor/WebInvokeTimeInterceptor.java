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
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * web 的调用时间统计拦截器
 */
public class WebInvokeTimeInterceptor implements HandlerInterceptor {

    private static final String stopWatchAttr = WebInvokeTimeInterceptor.class.getName() + ".stopWatch";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        // 请求参数：JSON 请求体要等 handler 读完才有内容（由 ContentCachingFilter 提供包装器），放到 afterCompletion 打印
        if (isJsonRequest(request)) {
            Logs.info(EmojiSymbol.WEB, "{}:{}", request.getMethod(), request.getRequestURI());
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
        request.setAttribute(stopWatchAttr, stopWatch);
        stopWatch.start();
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, @Nullable Exception ex) throws Exception {
        if (request instanceof ContentCachingRequestWrapper requestWrapper) {
            byte[] body = requestWrapper.getContentAsByteArray();
            Logs.info(EmojiSymbol.WEB, "{}:{},body:[{}]", request.getMethod(), request.getRequestURI(),
                    body.length == 0 ? "" : new String(body, StandardCharsets.UTF_8));
        }

        if (request.getAttribute(stopWatchAttr) instanceof StopWatch stopWatch) {
            stopWatch.stop();
            Logs.info(EmojiSymbol.WEB, "{}:{},take:[{}]ms", request.getMethod(), request.getRequestURI(), stopWatch.getTotalTimeMillis());
        }
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
