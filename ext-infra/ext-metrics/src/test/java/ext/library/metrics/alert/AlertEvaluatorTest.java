package ext.library.metrics.alert;

import ext.library.metrics.core.MetricsRegistry;
import ext.library.metrics.properties.MetricsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link AlertEvaluator} 的用例
 */
class AlertEvaluatorTest {

    private MutableClock clock;

    private MetricsRegistry registry;

    private MetricsProperties properties;

    private List<AlertEvent> events;

    private AlertEvaluator evaluator;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        properties = new MetricsProperties();
        properties.setWindow(Duration.ofSeconds(60));
        properties.getAlert().setInterval(Duration.ofSeconds(10));
        properties.getAlert().setCooldown(Duration.ofSeconds(60));
        registry = new MetricsRegistry(properties, clock);
        events = new ArrayList<>();
        evaluator = new AlertEvaluator(registry, properties, events::add, clock::millis);
    }

    @Test
    void skipsEvaluationBelowMinimumRequests() {
        properties.getAlert().setMinRequests(10);
        for (int i = 0; i < 5; i++) {
            registry.http("/api", 500, 10);
        }

        evaluator.run();

        assertEquals(0, events.size());
    }

    @Test
    void alertsWhenErrorRateExceedsThreshold() {
        properties.getAlert().setMinRequests(10);
        for (int i = 0; i < 10; i++) {
            registry.http("/api", i < 2 ? 500 : 200, 10);
        }

        evaluator.run();

        // 全局与单端点各报一条
        List<AlertEvent> fired = interlock("错误率");
        assertEquals(2, fired.size());
        assertEquals("global", fired.getFirst().target());
        assertEquals("20.00% > 1.00%", fired.getFirst().detail());
    }

    @Test
    void alertsWhenP99ExceedsThreshold() {
        properties.getAlert().setMinRequests(10);
        for (int i = 0; i < 9; i++) {
            registry.http("/api", 200, 10);
        }
        registry.http("/api", 200, 900);

        evaluator.run();

        assertEquals("P99 延迟", interlock("P99 延迟").getFirst().metric());
    }

    @Test
    void cooldownSuppressesRepeatedAlert() {
        properties.getAlert().setMinRequests(1);
        registry.http("/api", 500, 10);
        evaluator.run();

        clock.advance(Duration.ofSeconds(30));
        registry.http("/api", 500, 10);
        evaluator.run();

        assertEquals(2, interlock("错误率").size(), "冷却期内不重复报");

        clock.advance(Duration.ofSeconds(31));
        registry.http("/api", 500, 10);
        evaluator.run();

        assertEquals(4, interlock("错误率").size(), "冷却期满后重新告警");
    }

    @Test
    void customThresholdOverridesDefault() {
        properties.getAlert().setMinRequests(10);
        properties.getAlert().setErrorRateThreshold(0.9);
        for (int i = 0; i < 10; i++) {
            registry.http("/api", i == 0 ? 500 : 200, 10);
        }

        evaluator.run();

        assertEquals(0, events.size(), "错误率 10% 未超过 90% 阈值时不应告警");
    }

    private List<AlertEvent> interlock(String metric) {
        return events.stream().filter(event -> metric.equals(event.metric())).toList();
    }

    private static final class MutableClock extends Clock {

        private long millis = 1_700_000_000_000L;

        void advance(Duration duration) {
            millis += duration.toMillis();
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        @Override
        public long millis() {
            return millis;
        }
    }
}
