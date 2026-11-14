package ext.library.trace.config;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.trace.TraceContext;
import ext.library.trace.filter.TraceFilter;
import ext.library.trace.properties.TraceProperties;
import ext.library.trace.support.TraceInterceptor;
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
import org.springframework.boot.restclient.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.Filter;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * 链路追踪自动配置
 * <p>
 * MDC 跨线程传播不在这里：线程池只能挂一个 {@code TaskDecorator}，
 * 统一由 {@code ext-core} 的 {@code ContextPropagationAutoConfiguration} 处理。
 */
@AutoConfiguration
@EnableConfigurationProperties(TraceProperties.class)
public class TraceAutoConfiguration {

    /**
     * 关掉时只影响入站过滤器与出站头：{@code outboundHeaders()} 返回空，不再注入 traceparent
     */
    public TraceAutoConfiguration(TraceProperties properties) {
        TraceContext.setEnabled(properties.isEnabled());
    }

    @Bean
    @ConditionalOnProperty(prefix = TraceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
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

    /**
     * 给容器里的 {@link RestTemplate} 挂上出站追踪头
     * <p>
     * 只在还没有 {@link TraceInterceptor} 时追加：重复添加会让同一个头被写两次。
     * {@code RestClient} 走下面的 {@link RestClientCustomizer}。
     *
     * @return 后处理器
     */
    @Bean
    public static BeanPostProcessor traceRestTemplatePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName)
                    throws BeansException {
                if (bean instanceof RestTemplate template) {
                    boolean present = template.getInterceptors().stream()
                            .anyMatch(TraceInterceptor.class::isInstance);
                    if (!present) {
                        template.getInterceptors().add(new TraceInterceptor());
                    }
                }
                return bean;
            }
        };
    }

    /**
     * {@code RestClient} 与 {@code @HttpExchange} 接口的出站透传
     * <p>
     * 单独放嵌套类并加 {@link ConditionalOnClass}：没引 {@code spring-boot-restclient} 的服务
     * 不该因为缺这个类而启动失败。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RestClientCustomizer.class)
    static class RestClientTraceConfiguration {

        /**
         * 从注入的 {@code RestClient.Builder} 建出来的客户端都带上追踪头
         *
         * @return 定制器
         */
        @Bean
        @ConditionalOnMissingBean
        public RestClientCustomizer traceRestClientCustomizer() {
            return (RestClient.Builder builder) -> builder.requestInterceptor(new TraceInterceptor());
        }
    }

}
