package ext.library.metrics.core;

import ext.library.metrics.MetricsRecorder;
import ext.library.metrics.properties.MetricsProperties;
import ext.library.tool.trace.TraceContext;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Pattern;

/**
 * 指标注册表
 * <p>
 * 进程内聚合，不落盘、不推远端：读数靠 {@link #snapshot()}，告警靠 {@link #snapshot()} 的同一份数据。
 * 重启即归零，需要历史请把快照接出去。
 */
public class MetricsRegistry implements MetricsRecorder {

    /**
     * 拿不到路径模板时的兜底
     */
    private static final String UNKNOWN = "unknown";

    /**
     * SQL 字面量：单引号字符串与裸数字
     */
    private static final Pattern SQL_LITERAL = Pattern.compile("'(?:[^'\\\\]|\\\\.)*'|\\b\\d+\\b");

    private final MetricsProperties properties;

    private final Clock clock;

    private final Window global;

    private final Map<String, Window> endpoints = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> rateLimitRejected = new ConcurrentHashMap<>();

    private final Map<String, AtomicLong> idempotentHits = new ConcurrentHashMap<>();

    private final Deque<MetricsSnapshot.SlowSql> slowSql = new ArrayDeque<>();

    private final Map<String, MetricsSnapshot.Pool> pools = new ConcurrentHashMap<>();

    public MetricsRegistry(MetricsProperties properties) {
        this(properties, Clock.systemUTC());
    }

    public MetricsRegistry(MetricsProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.global = new Window(properties.getWindow().toMillis(), clock);
    }

    @Override
    public void http(String endpoint, int status, long millis) {
        boolean error = status >= 500;
        String name = endpoint == null || endpoint.isBlank() ? UNKNOWN : endpoint;
        endpointWindow(name).request(millis, error);
        global.request(millis, error);
    }

    @Override
    public void errorCode(String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        global.errorCode(code);
    }

    @Override
    public void rateLimitRejected(String name) {
        counter(rateLimitRejected, name).incrementAndGet();
    }

    @Override
    public void idempotentHit(String name) {
        counter(idempotentHits, name).incrementAndGet();
    }

    @Override
    public void slowSql(String sql, long millis) {
        if (sql == null || millis < properties.getSlowSqlThreshold().toMillis()) {
            return;
        }
        String text = SQL_LITERAL.matcher(sql).replaceAll("?");
        int maxLength = properties.getMaxSqlLength();
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength) + "...";
        }
        MetricsSnapshot.SlowSql entry = new MetricsSnapshot.SlowSql(text, millis, TraceContext.current());
        synchronized (slowSql) {
            slowSql.addLast(entry);
            while (slowSql.size() > properties.getMaxSlowSql()) {
                slowSql.removeFirst();
            }
        }
    }

    /**
     * 池水位由采样器定期覆盖，不累积历史
     */
    public void updatePool(MetricsSnapshot.Pool pool) {
        pools.put(pool.name(), pool);
    }

    /**
     * 取快照
     */
    public MetricsSnapshot snapshot() {
        Window.Stats stats = global.stats();

        List<MetricsSnapshot.Endpoint> endpointList = new ArrayList<>(endpoints.size());
        endpoints.forEach((name, window) -> {
            Window.Stats endpointStats = window.stats();
            endpointList.add(new MetricsSnapshot.Endpoint(name,
                                                         endpointStats.requests(),
                                                         endpointStats.errors(),
                                                         qps(endpointStats.requests(), endpointStats.elapsedMillis()),
                                                         errorRate(endpointStats.requests(), endpointStats.errors()),
                                                         endpointStats.p99Millis()));
        });
        endpointList.sort(Comparator.comparingLong(MetricsSnapshot.Endpoint::requests).reversed());

        List<MetricsSnapshot.SlowSql> slowSqlList;
        synchronized (slowSql) {
            slowSqlList = List.copyOf(slowSql);
        }

        return new MetricsSnapshot(properties.getWindow().toSeconds(),
                                   new MetricsSnapshot.Rates(stats.requests(),
                                                             stats.errors(),
                                                             qps(stats.requests(), stats.elapsedMillis()),
                                                             errorRate(stats.requests(), stats.errors()),
                                                             stats.p99Millis()),
                                   endpointList,
                                   counts(stats.errorCodes()),
                                   counts(rateLimitRejected),
                                   counts(idempotentHits),
                                   slowSqlList,
                                   List.copyOf(pools.values()));
    }

    /**
     * 端点表按 LRU 汰汰：满了就踢掉最久没访问的那个
     * <p>
     * 早期实现是「满了以后新名字全进 other」，但表只增不减，被扫描器刷满一次就永久停在 other 上。
     */
    private Window endpointWindow(String name) {
        Window window = endpoints.get(name);
        if (window != null) {
            return window;
        }
        if (endpoints.size() >= properties.getMaxEndpoints()) {
            evictLeastRecentlyUsed();
        }
        return endpoints.computeIfAbsent(name, ignored -> new Window(properties.getWindow().toMillis(), clock));
    }

    private void evictLeastRecentlyUsed() {
        long now = System.nanoTime();
        String oldest = null;
        long oldestIdle = -1;
        for (Map.Entry<String, Window> entry : endpoints.entrySet()) {
            long idle = entry.getValue().idleNanos(now);
            if (idle > oldestIdle) {
                oldestIdle = idle;
                oldest = entry.getKey();
            }
        }
        if (oldest != null) {
            endpoints.remove(oldest);
        }
    }

    private static AtomicLong counter(Map<String, AtomicLong> counters, String name) {
        return counters.computeIfAbsent(name == null || name.isBlank() ? UNKNOWN : name, ignored -> new AtomicLong());
    }

    /**
     * 每秒请求数：分母是窗口已过去的时长，不是整窗口
     */
    private static double qps(long requests, long elapsedMillis) {
        return elapsedMillis <= 0 ? 0 : requests * 1000.0 / elapsedMillis;
    }

    private static double errorRate(long requests, long errors) {
        return requests == 0 ? 0 : (double) errors / requests;
    }

    private static List<MetricsSnapshot.Count> counts(Map<String, ?> source) {
        List<MetricsSnapshot.Count> list = new ArrayList<>(source.size());
        source.forEach((name, value) -> list.add(new MetricsSnapshot.Count(name,
                switch (value) {
                    case AtomicLong counter -> counter.get();
                    case LongAdder counter -> counter.sum();
                    case Long count -> count;
                    default -> 0L;
                })));
        list.sort(Comparator.comparingLong(MetricsSnapshot.Count::count).reversed());
        return list;
    }
}
