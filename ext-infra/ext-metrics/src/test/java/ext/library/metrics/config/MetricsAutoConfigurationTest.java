package ext.library.metrics.config;

import ext.library.core.metrics.MetricsRecorder;
import ext.library.metrics.core.MetricsRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 自动装配的用例：零配置引入即生效
 */
class MetricsAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MetricsAutoConfiguration.class));

    @Test
    void registersEverythingWithoutConfiguration() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(MetricsRegistry.class);
            assertThat(context).hasSingleBean(MetricsRecorder.class);
            assertThat(context).hasSingleBean(MetricsScheduler.class);
            assertThat(context).hasBean("metricsFilterRegistration");
            assertThat(context).hasBean("extMetricsDashboard");
        });
    }

    @Test
    void canBeDisabledByProperty() {
        runner.withPropertyValues("ext.metrics.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(MetricsRegistry.class);
            assertThat(context).doesNotHaveBean("metricsFilterRegistration");
        });
    }

    @Test
    void dashboardCanBeTurnedOff() {
        runner.withPropertyValues("ext.metrics.dashboard.enabled=false").run(context ->
                assertThat(context).doesNotHaveBean("extMetricsDashboard"));
    }
}
