package ext.library.encrypt.handler;

import ext.library.encrypt.annotation.RequestDecrypt;
import ext.library.encrypt.enums.Algorithm;
import ext.library.encrypt.properties.CryptoProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.StringUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 请求解密处理器
 */
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(CryptoProperties.class)
@ControllerAdvice
public class RequestDecryptHandler extends RequestBodyAdviceAdapter {

    private final CryptoProperties cryptoProperties;

    public RequestDecryptHandler(CryptoProperties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(RequestDecrypt.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        String decryptStr = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());

        try {
            String secretKey = StringUtil.isBlank(cryptoProperties.getPrivateKey()) ? cryptoProperties.getSecretKey() : cryptoProperties.getPrivateKey();
            Algorithm algo = cryptoProperties.getAlgo();
            String decrypt = algo.getCryptoStrategy().decrypt(secretKey, decryptStr, cryptoProperties.getSalt());
            return new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                    return new ByteArrayInputStream(decrypt.getBytes(StandardCharsets.UTF_8));
                }

                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        } catch (Exception e) {
            throw new ExtException(EmojiSymbol.INTERFACE_CRYPTO, e);
        }

    }

}
