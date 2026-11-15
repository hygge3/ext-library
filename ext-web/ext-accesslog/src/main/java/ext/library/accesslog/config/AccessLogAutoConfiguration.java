package ext.library.accesslog.config;

import ext.library.accesslog.filter.AccessLogFilter;
import ext.library.accesslog.properties.AccessLogProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import jakarta.servlet.Filter;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * 访问日志自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(AccessLogProperties.class)
@ConditionalOnProperty(prefix = AccessLogProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class AccessLogAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnClass(Filter.class)
    @ConditionalOnMissingBean(name = "accessLogFilterRegistration")
    public FilterRegistrationBean<@NonNull AccessLogFilter> accessLogFilterRegistration(AccessLogProperties properties) {
        FilterRegistrationBean<@NonNull AccessLogFilter> bean = new FilterRegistrationBean<>(new AccessLogFilter(properties));
        bean.addUrlPatterns("/*");
        // traceFilter 在 +5，accesslog 稍后以便拿到 traceId
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        bean.setName("accessLogFilter");
        Logs.info(EmojiSymbol.WEB, "载入模块：访问日志");
        return bean;
    }
}
