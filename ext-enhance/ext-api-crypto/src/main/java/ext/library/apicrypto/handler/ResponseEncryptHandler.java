package ext.library.apicrypto.handler;

import ext.library.apicrypto.annotation.ResponseEncrypt;
import ext.library.apicrypto.enums.Algorithm;
import ext.library.apicrypto.properties.ApiCryptoProperties;
import ext.library.apicrypto.strategy.CryptoStrategy;
import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应加密处理器
 * <p>
 * 通过 Spring MVC 的 {@link ResponseBodyAdvice} 机制，
 * 在响应体写入前自动对数据进行加密。
 *
 * @since 4.0.0
 */
@ControllerAdvice
public class ResponseEncryptHandler implements ResponseBodyAdvice<Object> {

    private final ApiCryptoProperties properties;

    public ResponseEncryptHandler(ApiCryptoProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ResponseEncrypt.class)
                || returnType.getContainingClass().isAnnotationPresent(ResponseEncrypt.class);
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType,
                                            MediaType selectedContentType,
                                            Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                            ServerHttpRequest request,
                                            ServerHttpResponse response) {
        if (body == null) {
            return null;
        }

        try {
            Algorithm algorithm = determineAlgorithm(returnType);
            CryptoStrategy strategy = algorithm.getCryptoStrategy();
            String json = JsonUtil.toJson(body);

            return strategy.encrypt(properties.getEncryptKey(), json, properties.getSalt());
        } catch (Exception e) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, e);
        }
    }

    /**
     * 确定使用的加密算法（方法级注解优先于类级注解）
     */
    private Algorithm determineAlgorithm(MethodParameter returnType) {
        ResponseEncrypt annotation = returnType.getMethodAnnotation(ResponseEncrypt.class);
        if (annotation == null) {
            annotation = returnType.getContainingClass().getAnnotation(ResponseEncrypt.class);
        }
        if (annotation != null && !annotation.useDefault()) {
            return annotation.algorithm();
        }
        return properties.getAlgorithm();
    }
}
