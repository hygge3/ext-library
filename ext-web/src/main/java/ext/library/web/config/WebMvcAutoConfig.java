package ext.library.web.config;


import ext.library.tool.util.DateUtil;
import ext.library.web.body.resolver.BodyParamHandlerMethodArgumentResolver;
import ext.library.web.config.properties.WebMvcProperties;
import ext.library.web.interceptor.ExtWebInvokeTimeInterceptor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * Web MVC 自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties({WebMvcProperties.class})
@ConditionalOnWebApplication(type = SERVLET)
public class WebMvcAutoConfig implements WebMvcConfigurer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final WebMvcProperties webMvcProperties;

    public WebMvcAutoConfig(WebMvcProperties webMvcProperties) {
        this.webMvcProperties = webMvcProperties;
    }

    private static CorsConfiguration getCorsConfiguration(WebMvcProperties.@NonNull CorsConfig corsConfig) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsConfig.getAllowedOrigins());
        corsConfiguration.setAllowedOriginPatterns(corsConfig.getAllowedOriginPatterns());
        corsConfiguration.setAllowedMethods(corsConfig.getAllowedMethods());
        corsConfiguration.setAllowedHeaders(corsConfig.getAllowedHeaders());
        corsConfiguration.setExposedHeaders(corsConfig.getExposedHeaders());
        corsConfiguration.setAllowCredentials(corsConfig.getAllowCredentials());
        corsConfiguration.setMaxAge(corsConfig.getMaxAge());
        return corsConfiguration;
    }

    /**
     * 增加 GET 请求参数中时间类型转换
     *
     * <ul>
     * <li>HH:mm:ss -> LocalTime</li>
     * <li>yyyy-MM-dd -> LocalDate</li>
     * <li>yyyy-MM-dd HH:mm:ss -> LocalDateTime</li>
     * </ul>
     *
     * @param registry 注册表
     */
    @Override
    public void addFormatters(@NonNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setTimeFormatter(DateUtil.FORMATTER_HMS);
        registrar.setDateFormatter(DateUtil.FORMATTER_YMD);
        registrar.setDateTimeFormatter(DateUtil.FORMATTER_YMD_HMS);
        registrar.registerFormatters(registry);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        if (webMvcProperties.getInvokeTimeEnabled()) {
            log.info("[⏱️] 请求调用时间统计模块载入成功");
            // 全局访问性能拦截
            registry.addInterceptor(new ExtWebInvokeTimeInterceptor()).addPathPatterns("/**");
        }
    }

    /**
     * 添加资源处理程序，解决 resources 下面静态资源无法访问。
     *
     * @param registry 注册表
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath*:/static/").addResourceLocations("classpath*:/resources/").addResourceLocations("classpath*:/public/").addResourceLocations("classpath*:/META-INF/resources/");
    }

    /**
     * 注册自定义的 Body 参数解析器
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new BodyParamHandlerMethodArgumentResolver());
        WebMvcConfigurer.super.addArgumentResolvers(argumentResolvers);
    }

    /**
     * 允许聚合者对提供者的文档进行跨域访问 解决聚合文档导致的跨域问题
     *
     * @return FilterRegistrationBean
     */
    @Bean
    @ConditionalOnProperty(prefix = WebMvcProperties.PREFIX + ".cors", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<@NonNull CorsFilter> corsFilterRegistrationBean() {
        log.info("[🔛] CORS 模块载入成功");
        // 获取 CORS 配置
        WebMvcProperties.CorsConfig corsConfig = webMvcProperties.getCorsConfig();

        // 转换 CORS 配置
        CorsConfiguration corsConfiguration = getCorsConfiguration(corsConfig);

        // 注册 CORS 配置与资源的映射关系
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsConfig.getUrlPattern(), corsConfiguration);

        // 注册 CORS 过滤器，设置最高优先级 + 1 (在 traceId 之后)
        FilterRegistrationBean<@NonNull CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1000);

        return bean;
    }

}