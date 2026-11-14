package ext.library.metrics.config;

import ext.library.core.metrics.MetricsRecorder;
import ext.library.metrics.alert.AlertNotifier;
import ext.library.metrics.alert.LoggingAlertNotifier;
import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.core.RegistryMetricsRecorder;
import ext.library.metrics.jdbc.SqlTimingBeanPostProcessor;
import ext.library.metrics.pool.PoolSampler;
import ext.library.metrics.properties.MetricsProperties;
import ext.library.metrics.web.DashboardServlet;
import ext.library.metrics.web.MetricsFilter;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import jakarta.servlet.Filter;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/**
 * 指标与告警自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(MetricsProperties.class)
@ConditionalOnProperty(prefix = MetricsProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class MetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MetricsRegistry metricsRegistry(MetricsProperties properties) {
        Logs.info(EmojiSymbol.METRICS, "载入模块：运行时指标与告警");
        return new MetricsRegistry(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AlertNotifier alertNotifier() {
        return new LoggingAlertNotifier();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricsRecorder metricsRecorder(MetricsRegistry registry) {
        return new RegistryMetricsRecorder(registry);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.jdbc.core.JdbcTemplate")
    public static SqlTimingBeanPostProcessor sqlTimingBeanPostProcessor(ObjectProvider<MetricsRegistry> registry) {
        return new SqlTimingBeanPostProcessor(registry);
    }

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnClass(Filter.class)
    @ConditionalOnMissingBean(name = "metricsFilterRegistration")
    public FilterRegistrationBean<@NonNull MetricsFilter> metricsFilterRegistration(MetricsRegistry registry, MetricsProperties properties) {
        FilterRegistrationBean<@NonNull MetricsFilter> bean = new FilterRegistrationBean<>(new MetricsFilter(registry, properties));
        bean.addUrlPatterns("/*");
        // 在 trace（+5）与 accesslog（+20）之后：耗时含业务处理，traceId 也已绑定
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
        bean.setName("metricsFilter");
        return bean;
    }

    @Bean
    public MetricsScheduler metricsScheduler(MetricsRegistry registry, MetricsProperties properties, AlertNotifier notifier,
                                             ListableBeanFactory beanFactory) {
        return new MetricsScheduler(registry, properties, notifier, new PoolSampler(registry, beanFactory));
    }

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    @ConditionalOnClass(name = "jakarta.servlet.http.HttpServlet")
    @ConditionalOnProperty(prefix = MetricsProperties.PREFIX + ".dashboard", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ServletRegistrationBean<@NonNull DashboardServlet> extMetricsDashboard(MetricsRegistry registry, MetricsProperties properties) {
        String path = properties.getDashboard().getPath();
        ServletRegistrationBean<@NonNull DashboardServlet> bean =
                new ServletRegistrationBean<>(new DashboardServlet(registry, properties), path, path + "/*");
        bean.setName("extMetricsDashboard");
        Logs.info(EmojiSymbol.METRICS, "指标大屏：{}", path);
        return bean;
    }
}
