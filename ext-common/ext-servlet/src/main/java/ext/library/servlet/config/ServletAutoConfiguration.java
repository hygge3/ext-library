package ext.library.servlet.config;

import ext.library.servlet.filter.RepeatableRequestFilter;
import ext.library.servlet.properties.ServletProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * servlet 基座自动配置
 * <p>
 * 基座本身只提供静态类型，唯一的可装配能力是请求体缓存过滤器（默认关闭）。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(ServletProperties.class)
@ConditionalOnWebApplication(type = SERVLET)
public class ServletAutoConfiguration {

    /**
     * 请求体缓存过滤器
     *
     * @return 过滤器注册
     */
    @Bean
    @ConditionalOnProperty(prefix = ServletProperties.PREFIX + ".repeatable-request", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<@NonNull RepeatableRequestFilter> repeatableRequestFilter() {
        FilterRegistrationBean<@NonNull RepeatableRequestFilter> bean =
                new FilterRegistrationBean<>(new RepeatableRequestFilter());
        bean.addUrlPatterns("/*");
        // 尽可能早，保证下游所有读取方拿到的都是可重读的 body
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return bean;
    }

}
