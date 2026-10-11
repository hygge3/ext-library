package ext.library.apicrypto.handler;

import ext.library.apicrypto.annotation.RequestDecrypt;
import ext.library.apicrypto.enums.Algorithm;
import ext.library.apicrypto.properties.ApiCryptoProperties;
import ext.library.apicrypto.strategy.CryptoStrategy;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 请求解密处理器
 * <p>
 * 通过 Spring MVC 的 {@link RequestBodyAdviceAdapter} 机制，
 * 在请求体读取前自动对加密数据进行解密。
 *
 * @since 4.0.0
 */
@ControllerAdvice
public class RequestDecryptHandler extends RequestBodyAdviceAdapter {

    private final ApiCryptoProperties properties;

    public RequestDecryptHandler(ApiCryptoProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(RequestDecrypt.class)
                || methodParameter.getContainingClass().isAnnotationPresent(RequestDecrypt.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        String encryptedText = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);

        try {
            Algorithm algorithm = determineAlgorithm(parameter);
            CryptoStrategy strategy = algorithm.getCryptoStrategy();
            String decryptedText = strategy.decrypt(properties.getDecryptKey(), encryptedText, properties.getSalt());

            return new DecryptedHttpInputMessage(decryptedText, inputMessage.getHeaders());
        } catch (Exception e) {
            throw new ExtException(EmojiSymbol.API_CRYPTO, e);
        }
    }

    /**
     * 确定使用的加密算法（方法级注解优先于类级注解）
     */
    private Algorithm determineAlgorithm(MethodParameter parameter) {
        RequestDecrypt annotation = parameter.getMethodAnnotation(RequestDecrypt.class);
        if (annotation == null) {
            annotation = parameter.getContainingClass().getAnnotation(RequestDecrypt.class);
        }
        if (annotation != null && !annotation.useDefault()) {
            return annotation.algorithm();
        }
        return properties.getAlgorithm();
    }

    /**
     * 解密后的 HTTP 输入消息
     */
    private record DecryptedHttpInputMessage(String body, HttpHeaders headers) implements HttpInputMessage {

        @Override
        public InputStream getBody() {
            return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public HttpHeaders getHeaders() {
            return headers;
        }
    }
}
