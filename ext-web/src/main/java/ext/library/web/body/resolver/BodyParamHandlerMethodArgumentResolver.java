package ext.library.web.body.resolver;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import ext.library.json.util.JsonUtil;
import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import java.io.BufferedReader;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * BodyParam 参数解析器 实现 HandlerMethodArgumentResolver 接口
 */
@Slf4j
public class BodyParamHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {
    static final String POST = "post";
    static final String APPLICATION_JSON = "application/json";

    /**
     * 判断是否需要处理该参数
     *
     * @param parameter the method parameter to check
     * @return {@code true} if this resolver supports the supplied parameter;
     * {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(@Nonnull MethodParameter parameter) {
        // 只处理带有@BodyParam 注解的参数
        return parameter.hasParameterAnnotation(BodyParam.class);
    }

    @Override
    public Object resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String contentType = Objects.requireNonNull(request).getContentType();

        if ($.isNotEqual(contentType, APPLICATION_JSON)) {
            throw Exceptions.throwOut("解析参数异常，ContentType 需为 application/json");
        }

        // 解析字段
        BodyParam param = parameter.getParameterAnnotation(BodyParam.class);
        String paramName = $.defaultIfEmpty(param.value(), parameter.getParameterName());

        BufferedReader reader = request.getReader();
        Class<?> parameterType = parameter.getParameterType();

        JsonNode jsonNode = JsonUtil.readTree(request.getReader());

        Object result = JsonUtil.treeToObj(jsonNode.get(paramName), parameterType);

        if (jsonNode.isEmpty() || $.isNull(result)) {
            if (param.required()) {
                throw new MissingServletRequestParameterException(paramName, parameter.getNestedParameterType().getSimpleName());
            } else if ($.equals(ValueConstants.DEFAULT_NONE, param.defaultValue())) {
                throw new IllegalArgumentException($.format("参数解析异常，值为 null 时必须指定默认值", parameter.getNestedParameterType().getSimpleName(), paramName));
            } else {
                return $.convert(param.defaultValue(), parameterType);
            }
        }

        return result;
    }

}
