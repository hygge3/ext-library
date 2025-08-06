package ext.library.encrypt.handler;

import ext.library.crypto.AESUtil;
import ext.library.crypto.DESUtil;
import ext.library.crypto.RSAUtil;
import ext.library.crypto.SM2Util;
import ext.library.crypto.SM4Util;
import ext.library.encrypt.annotation.ResponseEncrypt;
import ext.library.encrypt.properties.CryptoProperties;
import ext.library.json.util.JsonUtil;
import ext.library.tool.core.Exceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 响应加密处理器
 */
@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class ResponseEncryptHandler implements ResponseBodyAdvice<Object> {

    final CryptoProperties cryptoProperties;

    @Override
    public boolean supports(@Nonnull MethodParameter returnType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ResponseEncrypt.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, @Nonnull MethodParameter returnType, @Nonnull MediaType selectedContentType, @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType, @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        // NULL 值不做加密处理
        if (body == null) {
            return null;
        }
        String json = JsonUtil.toJson(body);
        try {
            return switch (cryptoProperties.getAlgo()) {
                case RSA -> RSAUtil.encrypt(cryptoProperties.getPublicKey(), json);
                case SM2 -> SM2Util.encrypt(cryptoProperties.getPublicKey(), json);
                case AES -> AESUtil.encrypt(cryptoProperties.getSecretKey(), json, cryptoProperties.getSalt());
                case DES -> DESUtil.encrypt(cryptoProperties.getSecretKey(), json);
                case SM4 -> SM4Util.encryptByECB(cryptoProperties.getSecretKey(), json);
                case BASE64 -> Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            };
        } catch (Exception e) {
            log.error("响应加密异常", e);
            throw Exceptions.throwOut("响应加密异常，uri:{}", request.getURI().toString());
        }
    }

}