package ext.library.trace.config;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.trace.filter.TraceFilter;
import ext.library.trace.properties.TraceProperties;
import ext.library.trace.support.MdcTaskDecorator;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.servlet.Filter;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * 链路追踪自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(TraceProperties.class)
@ConditionalOnProperty(prefix = TraceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class TraceAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnClass(Filter.class)
    @ConditionalOnMissingBean(name = "traceFilterRegistration")
    public FilterRegistrationBean<@NonNull TraceFilter> traceFilterRegistration(TraceProperties properties) {
        FilterRegistrationBean<@NonNull TraceFilter> bean = new FilterRegistrationBean<>(new TraceFilter(properties));
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
        bean.setName("traceFilter");
        Logs.info(EmojiSymbol.CORE, "载入模块：链路追踪（traceId MDC + 响应头）");
        return bean;
    }

    @Bean
    @ConditionalOnMissingBean(name = "mdcTaskDecorator")
    @ConditionalOnProperty(prefix = TraceProperties.PREFIX, name = "propagate-mdc", havingValue = "true", matchIfMissing = true)
    public MdcTaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    /**
     * 为 Spring 线程池挂载 MDC 传播装饰器
     * <p>
     * 无公共 getter 判断是否已配置装饰器；这里直接设置。用户如需自定义装饰器，
     * 可关闭 {@code ext.trace.propagate-mdc} 或自行定义执行器 Bean。
     */
    @Bean
    @ConditionalOnProperty(prefix = TraceProperties.PREFIX, name = "propagate-mdc", havingValue = "true", matchIfMissing = true)
    public static BeanPostProcessor mdcTaskDecoratorPostProcessor(MdcTaskDecorator decorator) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
                if (bean instanceof ThreadPoolTaskExecutor executor) {
                    executor.setTaskDecorator(decorator);
                }
                return bean;
            }
        };
    }
}
