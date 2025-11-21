package ext.library.web.body.resolver;

import com.fasterxml.jackson.databind.JsonNode;
import ext.library.json.util.JsonNodeUtil;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import ext.library.tool.util.TypeCastUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * BodyParam 参数解析器 实现 HandlerMethodArgumentResolver 接口
 */
public class BodyParamHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * 判断是否需要处理该参数
     *
     * @param parameter the method parameter to check
     *
     * @return {@code true} if this resolver supports the supplied parameter;
     * {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        // 只处理带有@BodyParam 注解的参数
        return parameter.hasParameterAnnotation(BodyParam.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        String contentType = Objects.requireNonNull(request).getContentType();

        if (ObjectUtil.isNotEqual(contentType, MimeTypeUtils.APPLICATION_JSON_VALUE)) {
            throw new ExtException("[🌐] 解析参数异常，ContentType 需为 application/json");
        }

        // 解析字段
        BodyParam param = parameter.getParameterAnnotation(BodyParam.class);
        String paramName = ObjectUtil.defaultIfEmpty(param.value(), parameter.getParameterName());

        Class<?> parameterType = parameter.getParameterType();

        JsonNode jsonNode = JsonNodeUtil.toNode(request.getReader());

        if (jsonNode.isNull() || jsonNode.isEmpty()) {
            if (param.required()) {
                throw new MissingServletRequestParameterException(paramName, parameter.getNestedParameterType().getSimpleName());
            } else if (Objects.equals(ValueConstants.DEFAULT_NONE, param.defaultValue())) {
                throw new IllegalArgumentException(StringUtil.format("参数解析异常，{} 值为 null 时必须指定默认值", paramName));
            } else {
                return TypeCastUtil.cast(param.defaultValue(), parameterType);
            }
        }
        return JsonNodeUtil.getNodeValue(jsonNode, paramName, parameterType);
    }

}