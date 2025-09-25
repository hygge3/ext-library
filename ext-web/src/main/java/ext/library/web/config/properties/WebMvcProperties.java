package ext.library.web.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.cors.CorsConfiguration;

import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * web 属性
 */
@ConfigurationProperties(prefix = WebMvcProperties.PREFIX)
@Validated
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


    /** 是否打印启动信息 */
    private Boolean printStartupInfo = true;

    /** 需要 REST 包装软件包 */
    @Pattern(regexp = "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*(\\.\\*)?$")
    private String restPackage;

    /**
     * 跨域配置
     */
    private CorsConfig corsConfig;

    public String getTraceIdHeaderName() {
        return traceIdHeaderName;
    }

    public void setTraceIdHeaderName(String traceIdHeaderName) {
        this.traceIdHeaderName = traceIdHeaderName;
    }

    public Boolean getInvokeTimeEnabled() {
        return invokeTimeEnabled;
    }

    public void setInvokeTimeEnabled(Boolean invokeTimeEnabled) {
        this.invokeTimeEnabled = invokeTimeEnabled;
    }

    public Boolean getPrintStartupInfo() {
        return printStartupInfo;
    }

    public void setPrintStartupInfo(Boolean printStartupInfo) {
        this.printStartupInfo = printStartupInfo;
    }

    public String getRestPackage() {
        return restPackage;
    }

    public void setRestPackage(String restPackage) {
        this.restPackage = restPackage;
    }

    public CorsConfig getCorsConfig() {
        return corsConfig;
    }

    public void setCorsConfig(CorsConfig corsConfig) {
        this.corsConfig = corsConfig;
    }

    /**
     * <p>
     * 跨域配置。
     * </p>
     *
     * @see CorsConfiguration
     */
    public static class CorsConfig {

        /**
         * 开启 Cors 跨域配置
         */
        private final boolean enabled = false;

        /**
         * 跨域对应的 url 匹配规则
         */
        @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
        private final String urlPattern = "/**";
        /**
         * 允许跨域的方法列表
         */
        private final List<String> allowedMethods = List.of(CorsConfiguration.ALL);
        /**
         * 允许跨域的头信息
         */
        private final List<String> allowedHeaders = List.of(CorsConfiguration.ALL);
        /**
         * 额外允许跨域请求方获取的 response header 信息
         */
        private final List<String> exposedHeaders = List.of("X-Trace-Id");
        /**
         * 是否允许跨域发送 Cookie
         */
        private final Boolean allowCredentials = true;
        /**
         * 允许跨域的源
         */
        private List<String> allowedOrigins;
        /**
         * 允许跨域来源的匹配规则
         */
        private List<String> allowedOriginPatterns;
        /**
         * CORS 配置缓存时间，用于控制浏览器端是否发起 Option 预检请求。若配置此参数，在第一次获取到 CORS
         * 的配置信息后，在过期时间内，浏览器将直接发出请求，跳过 option 预检
         */
        private Long maxAge;

        public boolean isEnabled() {
            return enabled;
        }

        public String getUrlPattern() {
            return urlPattern;
        }

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public Boolean getAllowCredentials() {
            return allowCredentials;
        }

        public Long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(Long maxAge) {
            this.maxAge = maxAge;
        }
    }

}