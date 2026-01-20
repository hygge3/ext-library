package ext.library.apicrypto.config;

import ext.library.apicrypto.handler.RequestDecryptHandler;
import ext.library.apicrypto.handler.ResponseEncryptHandler;
import ext.library.apicrypto.properties.ApiCryptoProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * API 加解密自动配置类
 * <p>
 * 在 Spring MVC Web 应用中自动配置请求解密和响应加密功能。
 *
 * @since 4.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(HttpServletRequest.class)
@EnableConfigurationProperties(ApiCryptoProperties.class)
public class ApiCryptoAutoConfiguration {

    /**
     * 注册请求解密处理器
     *
     * @param properties API 加解密配置属性
     * @return 请求解密处理器
     */
    @Bean
    public RequestDecryptHandler requestDecryptHandler(ApiCryptoProperties properties) {
        return new RequestDecryptHandler(properties);
    }

    /**
     * 注册响应加密处理器
     *
     * @param properties API 加解密配置属性
     * @return 响应加密处理器
     */
    @Bean
    public ResponseEncryptHandler responseEncryptHandler(ApiCryptoProperties properties) {
        return new ResponseEncryptHandler(properties);
    }
}
