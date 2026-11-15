package ext.library.apicrypto.config;

import ext.library.apicrypto.handler.ApiSignHandler;
import ext.library.apicrypto.handler.RequestDecryptHandler;
import ext.library.apicrypto.handler.ResponseEncryptHandler;
import ext.library.apicrypto.properties.ApiCryptoProperties;
import ext.library.apicrypto.store.CacheNonceStore;
import ext.library.apicrypto.store.NonceStore;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 加解密自动配置类
 * <p>
 * 在 Spring MVC Web 应用中自动配置请求解密和响应加密功能。
 *
 * @since 4.0.0
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
// 按类名探测 servlet API：jakarta.servlet-api 由消费方容器提供，本模块以 compileOnly 引入
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
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

    // -----------------------接口签名----------------------------------

    /**
     * 随机数存储
     * <p>
     * 后端由 {@code ext.cache.cache-storage} 决定，本模块不再自建选型配置。
     *
     * @param cacheStrategy 缓存策略（ext-cache 提供）
     * @return 随机数存储
     */
    @Bean
    @ConditionalOnMissingBean
    public NonceStore nonceStore(CacheStrategy cacheStrategy) {
        Logs.info(EmojiSymbol.API_CRYPTO, "载入模块：防重放存储（存储策略: {}）", cacheStrategy.getClass().getSimpleName());
        return new CacheNonceStore(cacheStrategy);
    }

    /**
     * 接口签名拦截器注册
     *
     * @param nonceStore 随机数存储
     * @param properties 配置属性
     * @return 拦截器配置
     */
    @Bean
    @ConditionalOnProperty(prefix = ApiCryptoProperties.PREFIX + ".sign", name = "enabled",
            havingValue = "true", matchIfMissing = true)
    public WebMvcConfigurer apiSignConfigurer(NonceStore nonceStore, ApiCryptoProperties properties) {
        ApiSignHandler handler = new ApiSignHandler(nonceStore, properties);
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                // 显式靠后：顺序不能靠 bean 注册顺序碰运气，否则未登录请求也能消耗 nonce
                registry.addInterceptor(handler).addPathPatterns("/**").order(100);
            }
        };
    }
}
