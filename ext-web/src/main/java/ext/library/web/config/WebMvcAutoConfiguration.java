package ext.library.web.config;


import ext.library.tool.util.DateUtil;
import ext.library.web.config.properties.WebMvcProperties;
import ext.library.web.interceptor.ExtWebInvokeTimeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * Web MVC 自动配置
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties({WebMvcProperties.class})
@ConditionalOnWebApplication(type = SERVLET)
public class WebMvcAutoConfiguration implements WebMvcConfigurer {

    private final WebMvcProperties webMvcProperties;

    /**
     * 增加 GET 请求参数中时间类型转换
     * {@link ext.library.json.module.CustomJavaTimeModule}
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
    public void addFormatters(@NotNull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setTimeFormatter(DateUtil.FORMATTER_HMS);
        registrar.setDateFormatter(DateUtil.FORMATTER_YMD);
        registrar.setDateTimeFormatter(DateUtil.FORMATTER_YMD_HMS);
        registrar.registerFormatters(registry);
    }

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        if (webMvcProperties.getInvokeTimeEnabled()) {
            log.info("[⏱️] 请求调用时间统计拦截器");
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
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath*:/static/")
                .addResourceLocations("classpath*:/resources/")
                .addResourceLocations("classpath*:/public/")
                .addResourceLocations("classpath*:/META-INF/resources/");
    }

    /**
     * 允许聚合者对提供者的文档进行跨域访问 解决聚合文档导致的跨域问题
     *
     * @return FilterRegistrationBean
     */
    @Bean
    @ConditionalOnProperty(prefix = WebMvcProperties.PREFIX + ".cors", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        log.info("[🔛] CORS ");
        // 获取 CORS 配置
        WebMvcProperties.CorsConfig corsConfig = webMvcProperties.getCorsConfig();

        // 转换 CORS 配置
        CorsConfiguration corsConfiguration = getCorsConfiguration(corsConfig);

        // 注册 CORS 配置与资源的映射关系
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(corsConfig.getUrlPattern(), corsConfiguration);

        // 注册 CORS 过滤器，设置最高优先级 + 1 (在 traceId 之后)
        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1000);

        return bean;
    }

    private static CorsConfiguration getCorsConfiguration(WebMvcProperties.CorsConfig corsConfig) {
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

}
