package ext.library.encrypt.handler;

import ext.library.encrypt.annotation.ResponseEncrypt;
import ext.library.encrypt.properties.CryptoProperties;
import ext.library.encrypt.util.AESUtil;
import ext.library.encrypt.util.RSAUtil;
import ext.library.json.util.JsonUtil;
import ext.library.tool.core.Exceptions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 响应加密处理器
 */
@ControllerAdvice
@RequiredArgsConstructor
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class ResponseEncryptHandler implements ResponseBodyAdvice<Object> {

    final CryptoProperties cryptoProperties;

    @Override
    public boolean supports(@Nonnull MethodParameter returnType,
                            @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ResponseEncrypt.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, @Nonnull MethodParameter returnType,
                                  @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType, @Nonnull ServerHttpRequest request,
                                  @Nonnull ServerHttpResponse response) {
        // NULL 值不做加密处理
        if (body == null) {
            return null;
        }
        String json = JsonUtil.toJson(body);
        try {
            return switch (cryptoProperties.getAlgo()) {
                case AES -> new String(AESUtil.ecbEncrypt(json.getBytes(), cryptoProperties.getPublicKey().getBytes()));
                case RSA -> RSAUtil.encryptByPublicKey(json, cryptoProperties.getPublicKey());
            };
        } catch (Exception e) {
            throw Exceptions.throwOut("响应加密异常，uri:{}", request.getURI().toString());
        }
    }

}