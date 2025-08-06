package ext.library.encrypt.handler;

import ext.library.crypto.AESUtil;
import ext.library.crypto.DESUtil;
import ext.library.crypto.RSAUtil;
import ext.library.crypto.SM2Util;
import ext.library.crypto.SM4Util;
import ext.library.encrypt.annotation.RequestDecrypt;
import ext.library.encrypt.enums.Algorithm;
import ext.library.encrypt.properties.CryptoProperties;
import ext.library.tool.core.Exceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 请求解密处理器
 */
@RequiredArgsConstructor
@Slf4j
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(CryptoProperties.class)
@ControllerAdvice
public class RequestDecryptHandler extends RequestBodyAdviceAdapter {

    final CryptoProperties cryptoProperties;

    @Override
    public boolean supports(@Nonnull MethodParameter methodParameter, @Nonnull Type targetType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(RequestDecrypt.class);
    }

    @Nonnull
    @Override
    public HttpInputMessage beforeBodyRead(@Nonnull HttpInputMessage inputMessage, @Nonnull MethodParameter parameter, @Nonnull Type targetType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        String decryptStr = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());

        try {
            Algorithm algo = cryptoProperties.getAlgo();
            String decrypt = switch (algo) {
                case RSA -> RSAUtil.decrypt(cryptoProperties.getPrivateKey(), decryptStr);
                case SM2 -> SM2Util.decrypt(cryptoProperties.getPrivateKey(), decryptStr);
                case AES -> AESUtil.decrypt(cryptoProperties.getSecretKey(), decryptStr, cryptoProperties.getSalt());
                case DES -> DESUtil.decrypt(cryptoProperties.getSecretKey(), decryptStr);
                case SM4 -> SM4Util.decryptByECB(cryptoProperties.getSecretKey(), decryptStr);
                case BASE64 -> new String(Base64.getDecoder().decode(decryptStr), StandardCharsets.UTF_8);
            };
            return new HttpInputMessage() {
                @Nonnull
                @Override
                public InputStream getBody() throws IOException {
                    return new ByteArrayInputStream(decrypt.getBytes(StandardCharsets.UTF_8));
                }

                @Nonnull
                @Override
                public HttpHeaders getHeaders() {
                    return inputMessage.getHeaders();
                }
            };
        } catch (Exception e) {
            log.error("请求解密异常", e);
            throw Exceptions.throwOut("请求解密异常");
        }

    }

}