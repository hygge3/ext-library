package ext.library.web.handler;


import ext.library.json.util.JsonUtil;
import ext.library.tool.$;
import ext.library.web.annotation.RestWrapper;
import ext.library.web.response.R;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应结果处理器。
 * <p>
 * 标准 HTTP 状态码
 */
@Slf4j
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否要执行 beforeBodyWrite 方法.true 为执行，false 不执行，有注解标记的时候处理返回值
     */
    @Override
    public boolean supports(@NotNull MethodParameter returnType, @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        // 不需要包装
        if (!returnType.hasMethodAnnotation(RestWrapper.class) && !returnType.getContainingClass().isAnnotationPresent(RestWrapper.class)) {
            return false;
        }
        RestWrapper wrapper = returnType.getMethodAnnotation(RestWrapper.class);
        return wrapper.wrap() && wrapper.value();
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
     * @return {@link Object }
     */
    @Override
    public Object beforeBodyWrite(@Nullable Object body, @NotNull MethodParameter returnType, @NotNull MediaType selectedContentType, @NotNull Class<? extends HttpMessageConverter<?>> selectedConverterType, @NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response) {
        if ($.isNull(body)) {
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
