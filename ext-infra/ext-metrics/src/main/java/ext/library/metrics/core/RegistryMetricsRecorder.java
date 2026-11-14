package ext.library.metrics.core;

import ext.library.core.metrics.MetricsRecorder;

/**
 * 把 {@link MetricsRecorder} 的调用接到 {@link MetricsRegistry}
 */
public class RegistryMetricsRecorder implements MetricsRecorder {

    private final MetricsRegistry registry;

    public RegistryMetricsRecorder(MetricsRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void rejected(String name) {
        registry.rateLimitRejected(name);
    }

    @Override
    public void deduplicated(String name) {
        registry.idempotentHit(name);
    }

    @Override
    public void businessError(String code) {
        registry.errorCode(code);
    }

    @Override
    public void slowQuery(String sql, long millis) {
        registry.slowSql(sql, millis);
    }
}
