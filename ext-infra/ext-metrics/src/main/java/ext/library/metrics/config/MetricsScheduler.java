package ext.library.metrics.config;

import ext.library.metrics.alert.AlertEvaluator;
import ext.library.metrics.alert.AlertNotifier;
import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.pool.PoolSampler;
import ext.library.metrics.properties.MetricsProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 指标后台调度
 * <p>
 * 一个守护线程跑两件事：告警评估、池水位采样。用自有线程而不是 Spring 的 {@code TaskScheduler}，
 * 免得跟消费方的调度配置互相影响；任务内部异常只记 WARN，不中断后续调度。
 */
public class MetricsScheduler implements AutoCloseable {

    private final ScheduledExecutorService scheduler;

    public MetricsScheduler(MetricsRegistry registry, MetricsProperties properties, AlertNotifier notifier, PoolSampler sampler) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "ext-metrics");
            thread.setDaemon(true);
            return thread;
        });
        if (properties.getAlert().isEnabled()) {
            scheduleEvery(new AlertEvaluator(registry, properties, notifier), properties.getAlert().getInterval());
        }
        if (properties.getPool().isEnabled() && sampler != null) {
            scheduleEvery(sampler, properties.getPool().getInterval());
        }
    }

    private void scheduleEvery(Runnable task, Duration interval) {
        long millis = Math.max(100, interval.toMillis());
        scheduler.scheduleWithFixedDelay(() -> guard(task), millis, millis, TimeUnit.MILLISECONDS);
    }

    private static void guard(Runnable task) {
        try {
            task.run();
        } catch (RuntimeException e) {
            Logs.warn(EmojiSymbol.METRICS, e, "指标后台任务执行失败");
        }
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
