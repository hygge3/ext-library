package ext.library.web.handler;


import ext.library.json.util.JsonUtil;
import ext.library.tool.$;
import ext.library.tool.constant.Symbol;
import ext.library.web.annotation.RestWrapper;
import ext.library.web.config.properties.WebMvcProperties;
import ext.library.web.response.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.annotation.Nonnull;

/**
 * 响应结果处理器。
 * <p>
 * 标准 HTTP 状态码
 */
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {

    final WebMvcProperties webMvcProperties;

    /**
     * 判断是否要执行 beforeBodyWrite 方法.true 为执行，false 不执行，有注解标记的时候处理返回值
     */
    @Override
    public boolean supports(@Nonnull MethodParameter returnType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        // 需要包装的：指定包下；RestWrapper 注解

        // 不需要包装
        if (!returnType.hasMethodAnnotation(RestWrapper.class) && !returnType.getContainingClass().isAnnotationPresent(RestWrapper.class)) {
            String restPackage = webMvcProperties.getRestPackage();
            if (StringUtils.hasText(restPackage)) {
                if (restPackage.equals(Symbol.ASTERISK)) {
                    return true;
                }
                // 不含注解但是是指定包获取包名
                String packageName = returnType.getParameterType().getPackage().getName();
                return packageName.matches(restPackage);
            } else {
                return false;
            }
        }
        RestWrapper wrapper = returnType.getMethodAnnotation(RestWrapper.class);
        wrapper = $.defaultIfNull(wrapper, returnType.getContainingClass().getAnnotation(RestWrapper.class));
        if ($.isNull(wrapper)) {
            return false;
        }
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
     *
     * @return {@link Object }
     */
    @Override
    public Object beforeBodyWrite(Object body, @Nonnull MethodParameter returnType, @Nonnull MediaType selectedContentType, @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType, @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
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