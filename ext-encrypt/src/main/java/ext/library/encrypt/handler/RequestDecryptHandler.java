package ext.library.encrypt.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import jakarta.servlet.http.HttpServletRequest;

import com.google.common.base.Charsets;
import ext.library.encrypt.annotation.RequestDecrypt;
import ext.library.encrypt.enums.Algorithm;
import ext.library.encrypt.properties.CryptoProperties;
import ext.library.encrypt.util.AESUtil;
import ext.library.encrypt.util.RSAUtil;
import ext.library.tool.core.Exceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * 请求解密处理器
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class RequestDecryptHandler extends RequestBodyAdviceAdapter {

    final CryptoProperties cryptoProperties;

    @Override
    public boolean supports(MethodParameter methodParameter, @NotNull Type targetType,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(RequestDecrypt.class);
    }

    @Override
    public @NotNull HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, @NotNull MethodParameter parameter,
                                                    @NotNull Type targetType, @NotNull Class<? extends HttpMessageConverter<?>> converterType)
            throws IOException {
        String decryptStr = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());

        try {
            Algorithm algo = cryptoProperties.getAlgo();
            String decrypt = switch (algo) {
                case AES:
                    yield new String(AESUtil.ecbDecrypt(decryptStr.getBytes(), cryptoProperties.getSecretKey().getBytes()));
                case RSA:
                    yield RSAUtil.decryptByPrivateKey(decryptStr, cryptoProperties.getSecretKey());
            };
            return new HttpInputMessage() {
                @NotNull
                @Override
                public InputStream getBody() throws IOException {
                    return new ByteArrayInputStream(decrypt.getBytes(Charsets.UTF_8));
                }

                @NotNull
                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        } catch (Exception e) {
            throw Exceptions.throwOut("请求解密异常");
        }

    }

}
