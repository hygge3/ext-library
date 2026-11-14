package ext.library.metrics.core;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 固定统计窗口
 * <p>
 * 请求数、错误数、延迟直方图、错误码分布同生共死：窗口到期一起清零，保证快照里几个数字是同一段时间的。
 * <p>
 * 计数全部无锁累加：这里是每个请求都要过的热路径，一把全局锁会把监控开销加到所有接口上。
 * 滚动窗口只在到期那一刻加锁，滚动期间正在累加的少量样本会丢，指标不要求精确到个位。
 */
final class Window {

    private final long windowMillis;

    private final Clock clock;

    private final LatencyHistogram latency = new LatencyHistogram();

    private final Map<String, LongAdder> errorCodes = new ConcurrentHashMap<>();

    private final LongAdder requests = new LongAdder();

    private final LongAdder errors = new LongAdder();

    private final ReentrantLock rolloverLock = new ReentrantLock();

    private volatile long startMillis;

    /**
     * 最近一次被访问的时刻，端点表按它做 LRU 汰汰
     */
    private volatile long lastAccessNanos = System.nanoTime();

    Window(long windowMillis, Clock clock) {
        this.windowMillis = windowMillis;
        this.clock = clock;
        this.startMillis = clock.millis();
    }

    void request(long millis, boolean error) {
        rollover(clock.millis());
        lastAccessNanos = System.nanoTime();
        requests.increment();
        if (error) {
            errors.increment();
        }
        latency.record(millis);
    }

    void errorCode(String code) {
        rollover(clock.millis());
        lastAccessNanos = System.nanoTime();
        errorCodes.computeIfAbsent(code, ignored -> new LongAdder()).increment();
    }

    Stats stats() {
        long now = clock.millis();
        rollover(now);
        lastAccessNanos = System.nanoTime();
        Map<String, Long> codes = new HashMap<>();
        errorCodes.forEach((code, counter) -> codes.put(code, counter.sum()));
        return new Stats(requests.sum(), errors.sum(), latency.percentile(0.99), codes, Math.max(1, now - startMillis));
    }

    /**
     * 距上次访问过去的纳秒数，端点表汰汰用
     */
    long idleNanos(long nowNanos) {
        return nowNanos - lastAccessNanos;
    }

    /**
     * 当前窗口已经过去的毫秒数
     * <p>
     * 窗口刚滚动时只有几百毫秒的样本，拿整窗口做分母会把 QPS 压低几十倍。
     */
    long elapsedMillis() {
        return Math.max(1, clock.millis() - startMillis);
    }

    /**
     * 窗口到期就整体清零
     * <p>
     * 只在读写时按需滚动，没有后台线程。长时间无流量后第一次访问会直接开新窗口，
     * 中间空窗不补零——补零会让 QPS 分母变大、把突刺抹平。
     */
    private void rollover(long now) {
        if (now - startMillis < windowMillis) {
            return;
        }
        rolloverLock.lock();
        try {
            if (now - startMillis < windowMillis) {
                return;
            }
            startMillis = now;
            requests.reset();
            errors.reset();
            latency.reset();
            errorCodes.clear();
        } finally {
            rolloverLock.unlock();
        }
    }

    record Stats(long requests, long errors, long p99Millis, Map<String, Long> errorCodes, long elapsedMillis) {
    }
}
