package ext.library.encrypt.handler;

import ext.library.encrypt.annotation.ResponseEncrypt;
import ext.library.encrypt.enums.Algorithm;
import ext.library.encrypt.properties.CryptoProperties;
import ext.library.json.util.JsonUtil;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.StringUtil;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(CryptoProperties.class)
public class ResponseEncryptHandler implements ResponseBodyAdvice<Object> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CryptoProperties cryptoProperties;

    public ResponseEncryptHandler(CryptoProperties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ResponseEncrypt.class);
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        // NULL 值不做加密处理
        if (body == null) {
            return null;
        }
        String json = JsonUtil.toJson(body);
        try {
            String secretKey = StringUtil.isBlank(cryptoProperties.getPublicKey()) ? cryptoProperties.getSecretKey() : cryptoProperties.getPublicKey();
            Algorithm algo = cryptoProperties.getAlgo();
            return algo.getCryptoStrategy().encrypt(secretKey, json, cryptoProperties.getSalt());
        } catch (Exception e) {
            log.error("[🔒] 响应加密异常", e);
            throw new ExtException(e);
        }
    }

}