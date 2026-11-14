package ext.library.metrics.core;

import ext.library.metrics.properties.MetricsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MetricsRegistry} 的用例
 */
class MetricsRegistryTest {

    private MutableClock clock;

    private MetricsProperties properties;

    private MetricsRegistry registry;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        properties = new MetricsProperties();
        properties.setWindow(Duration.ofSeconds(60));
        registry = new MetricsRegistry(properties, clock);
    }

    @Test
    void aggregatesRequestsErrorsAndP99() {
        registry.http("/api/user/{id}", 200, 10);
        registry.http("/api/user/{id}", 200, 10);
        registry.http("/api/user/{id}", 500, 900);
        // QPS 的分母是窗口已过去的时长，不是整窗口
        clock.advance(Duration.ofSeconds(30));

        MetricsSnapshot snapshot = registry.snapshot();

        assertEquals(3, snapshot.rates().requests());
        assertEquals(1, snapshot.rates().errors());
        assertEquals(1.0 / 3, snapshot.rates().errorRate(), 1e-9);
        assertEquals(3 * 1000.0 / 30_000, snapshot.rates().qps(), 1e-9);
        assertEquals(1_000, snapshot.rates().p99Millis());
        assertEquals(1, snapshot.endpoints().size());
        assertEquals("/api/user/{id}", snapshot.endpoints().getFirst().name());
    }

    @Test
    void evictsLeastRecentlyUsedEndpointInsteadOfOverflowing() {
        properties.setMaxEndpoints(2);
        registry = new MetricsRegistry(properties, clock);

        registry.http("/a", 200, 1);
        registry.http("/b", 200, 1);
        // /a 被重新访问，最久未动的变成 /b
        registry.http("/a", 200, 1);
        registry.http("/c", 200, 1);

        List<String> names = registry.snapshot().endpoints().stream().map(MetricsSnapshot.Endpoint::name).toList();
        assertTrue(names.contains("/a") && names.contains("/c"), "新端点应挤掉最久未访问的那个：" + names);
        assertFalse(names.contains("/b"), "最久未访问的端点应被汰汰：" + names);
    }

    @Test
    void separatesBusinessErrorCodesFromHttpErrors() {
        registry.http("/api", 200, 5);
        registry.errorCode("40001");
        registry.errorCode("40001");
        registry.errorCode("50000");

        MetricsSnapshot snapshot = registry.snapshot();

        // 业务错误码不计入 5xx 错误率
        assertEquals(0, snapshot.rates().errors());
        assertEquals("40001", snapshot.errorCodes().getFirst().name());
        assertEquals(2, snapshot.errorCodes().getFirst().count());
    }

    @Test
    void countsRateLimitRejectionsAndIdempotentHits() {
        registry.rateLimitRejected("login");
        registry.rateLimitRejected("login");
        registry.idempotentHit("OrderController#create");

        MetricsSnapshot snapshot = registry.snapshot();

        assertEquals(2, snapshot.rateLimitRejected().getFirst().count());
        assertEquals("login", snapshot.rateLimitRejected().getFirst().name());
        assertEquals(1, snapshot.idempotentHits().getFirst().count());
    }

    @Test
    void recordsOnlySlowSqlAndMasksLiterals() {
        properties.setSlowSqlThreshold(Duration.ofMillis(500));

        registry.slowSql("select * from t_order where id = 42 and name = 'tom'", 100);
        registry.slowSql("select * from t_order where id = 42 and name = 'tom'", 800);

        MetricsSnapshot snapshot = registry.snapshot();

        assertEquals(1, snapshot.slowSql().size());
        assertEquals("select * from t_order where id = ? and name = ?", snapshot.slowSql().getFirst().sql());
        assertEquals(800, snapshot.slowSql().getFirst().millis());
    }

    @Test
    void keepsOnlyNewestSlowSqlEntries() {
        properties.setSlowSqlThreshold(Duration.ofMillis(1));
        properties.setMaxSlowSql(2);

        registry.slowSql("select 1", 10);
        registry.slowSql("select 2", 20);
        registry.slowSql("select 3", 30);

        MetricsSnapshot snapshot = registry.snapshot();

        assertEquals(2, snapshot.slowSql().size());
        assertEquals("select ?", snapshot.slowSql().getLast().sql());
    }

    @Test
    void windowRolloverResetsCounters() {
        registry.http("/api", 200, 10);
        assertEquals(1, registry.snapshot().rates().requests());

        clock.advance(Duration.ofSeconds(61));

        assertEquals(0, registry.snapshot().rates().requests());
    }

    @Test
    void poolWatermarksReplaceInsteadOfAccumulate() {
        registry.updatePool(new MetricsSnapshot.Pool("taskExecutor", "线程池", 1, 2, 8, 0, 100));
        registry.updatePool(new MetricsSnapshot.Pool("taskExecutor", "线程池", 3, 4, 8, 1, 100));

        MetricsSnapshot snapshot = registry.snapshot();

        assertEquals(1, snapshot.pools().size());
        assertEquals(3, snapshot.pools().getFirst().active());
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
