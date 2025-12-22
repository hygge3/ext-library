package ext.library.web.handler;


import ext.library.json.util.JsonUtil;
import ext.library.web.annotation.IgnoreRestWrapper;
import ext.library.web.properties.WebMvcProperties;
import ext.library.web.response.R;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * 响应结果处理器。
 * <p>
 * 标准 HTTP 状态码
 */
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    private final WebMvcProperties webMvcProperties;

    public GlobalResponseHandler(WebMvcProperties webMvcProperties) {
        this.webMvcProperties = webMvcProperties;
    }

    /**
     * 判断是否要执行 beforeBodyWrite 方法.true 为执行，false 不执行，有注解标记的时候处理返回值
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 需要包装的：开启且没有 IgnoreRestWrapper 注解
        if (!webMvcProperties.getRestWrapper()) {
            return false;
        }
        if (returnType.hasMethodAnnotation(IgnoreRestWrapper.class)) {
            return false;
        }
        return !returnType.getContainingClass().isAnnotationPresent(IgnoreRestWrapper.class);
    }

    /**
     * 对返回值做包装处理。
     *
     * @param body                  返回内容
     * @param returnType            返回类型
     * @param selectedContentType   所选内容类型
     * @param selectedConverterType 选定转换器类型
     * @param request               要求
     * @param response              回答
     *
     * @return {@link Object }
     */
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (Objects.isNull(body)) {
            return R.ok();
        }
        if (body instanceof R<?>) {
            return body;
        } else if (body instanceof String) {
            // String 特殊处理
            return JsonUtil.toJson(R.ok(body));
        } else {
            return R.ok(body);
        }
    }

}
