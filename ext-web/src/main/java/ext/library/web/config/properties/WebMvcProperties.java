package ext.library.web.config.properties;

import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.cors.CorsConfiguration;

/**
 * web 属性
 */
@Data
@ConfigurationProperties(prefix = WebMvcProperties.PREFIX)
public class WebMvcProperties {

    public static final String PREFIX = "ext.web";

    /**
     * traceId 的 http 头名称
     */
    private String traceIdHeaderName = "X-Trace-Id";

    /**
     * 是否启用调用时间统计拦截器
     */
    private Boolean invokeTimeEnabled = false;

    /**
     * 跨域配置
     */
    private CorsConfig corsConfig;

    /**
     * <p>
     * 跨域配置。
     * </p>
     *
     * @see CorsConfiguration
     */
    @Data
    public static class CorsConfig {

        /**
         * 开启 Cors 跨域配置
         */
        private boolean enabled = false;

        /**
         * 跨域对应的 url 匹配规则
         */
        private String urlPattern = "/**";

        /**
         * 允许跨域的源
         */
        private List<String> allowedOrigins;

        /**
         * 允许跨域来源的匹配规则
         */
        private List<String> allowedOriginPatterns;

        /**
         * 允许跨域的方法列表
         */
        private List<String> allowedMethods = List.of(CorsConfiguration.ALL);

        /**
         * 允许跨域的头信息
         */
        private List<String> allowedHeaders = List.of(CorsConfiguration.ALL);

        /**
         * 额外允许跨域请求方获取的 response header 信息
         */
        private List<String> exposedHeaders = List.of("traceId");

        /**
         * 是否允许跨域发送 Cookie
         */
        private Boolean allowCredentials = true;

        /**
         * CORS 配置缓存时间，用于控制浏览器端是否发起 Option 预检请求。若配置此参数，在第一次获取到 CORS
         * 的配置信息后，在过期时间内，浏览器将直接发出请求，跳过 option 预检
         */
        private Long maxAge;

    }

}
