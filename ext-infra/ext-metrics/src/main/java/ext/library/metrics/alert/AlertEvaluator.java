package ext.library.metrics.alert;

import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.core.MetricsSnapshot;
import ext.library.metrics.properties.MetricsProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * 告警评估器
 * <p>
 * 定时读一次快照，全局和每个端点都按同一套阈值判，命中的交给 {@link AlertNotifier}。
 * 同一对象同一指标在冷却期内只报一次，否则 10 秒一次的评估会把日志刷满。
 */
public class AlertEvaluator implements Runnable {

    private final MetricsRegistry registry;

    private final MetricsProperties properties;

    private final AlertNotifier notifier;

    private final LongSupplier clock;

    private final Map<String, Long> lastFired = new ConcurrentHashMap<>();

    public AlertEvaluator(MetricsRegistry registry, MetricsProperties properties, AlertNotifier notifier) {
        this(registry, properties, notifier, System::currentTimeMillis);
    }

    public AlertEvaluator(MetricsRegistry registry, MetricsProperties properties, AlertNotifier notifier, LongSupplier clock) {
        this.registry = registry;
        this.properties = properties;
        this.notifier = notifier;
        this.clock = clock;
    }

    @Override
    public void run() {
        MetricsSnapshot snapshot = registry.snapshot();
        long minRequests = properties.getAlert().getMinRequests();

        evaluate(GLOBAL, snapshot.rates().requests(), snapshot.rates().errorRate(), snapshot.rates().p99Millis(), minRequests);
        for (MetricsSnapshot.Endpoint endpoint : snapshot.endpoints()) {
            if (GLOBAL.equals(endpoint.name())) {
                continue;
            }
            evaluate(endpoint.name(), endpoint.requests(), endpoint.errorRate(), endpoint.p99Millis(), minRequests);
        }
    }

    private static final String GLOBAL = "global";

    private void evaluate(String target, long requests, double errorRate, long p99Millis, long minRequests) {
        // 样本太少时分位与比率都没意义，低流量接口会天天误报
        if (requests < minRequests) {
            return;
        }
        double errorRateThreshold = properties.getAlert().getErrorRateThreshold();
        if (errorRate > errorRateThreshold) {
            fire("错误率", target, String.format(Locale.ROOT, "%.2f%% > %.2f%%", errorRate * 100, errorRateThreshold * 100));
        }
        long p99Threshold = properties.getAlert().getP99Threshold().toMillis();
        if (p99Millis > p99Threshold) {
            fire("P99 延迟", target, String.format(Locale.ROOT, "%dms > %dms", p99Millis, p99Threshold));
        }
    }

    private void fire(String metric, String target, String detail) {
        long now = clock.getAsLong();
        String key = metric + ':' + target;
        Long last = lastFired.get(key);
        if (last != null && now - last < properties.getAlert().getCooldown().toMillis()) {
            return;
        }
        lastFired.put(key, now);
        try {
            notifier.notify(new AlertEvent(metric, target, detail, Instant.ofEpochMilli(now)));
        } catch (RuntimeException e) {
            // 通知渠道挂了不能连带定位信息一起丢
            Logs.warn(EmojiSymbol.METRICS, e, "指标告警发送失败：{} {} → {}", metric, target, detail);
        }
    }
}
