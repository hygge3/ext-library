package ext.library.ratelimit.store;

import ext.library.ratelimit.RateLimitResult;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存限流存储
 * <p>
 * 固定窗口计数，仅在单实例部署下有效。生产环境请使用 Redis 或 PostgreSQL 实现。
 */
public class MemoryRateLimitStore implements RateLimitStore {

    /**
     * 清扫过期计数器的调用间隔（每 N 次 increment 扫一次）
     * <p>
     * shortcut: 过期条目靠每 {@value #SWEEP_INTERVAL} 次调用清扫一次，内存上限约等于「一个窗口内出现过的 key 数」，
     * 需要按 key 精确回收时换成带过期策略的本地缓存
     */
    private static final int SWEEP_INTERVAL = 1024;

    /** 计数表：key → 窗口计数器 */
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    /** 调用计数，用于触发周期性清扫 */
    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public RateLimitResult increment(String key, int limit, Duration window) {
        Counter counter = counters.compute(key, (k, existing) ->
                existing == null || existing.isStale() ? new Counter(window) : existing);
        int count = counter.increment();

        if (calls.incrementAndGet() % SWEEP_INTERVAL == 0) {
            sweepExpired();
        }

        return new RateLimitResult(count <= limit, count, Math.max(0, limit - count), limit, counter.remaining());
    }

    /**
     * 清除已结束窗口的计数器
     */
    void sweepExpired() {
        counters.values().removeIf(Counter::isStale);
    }

    /**
     * @return 当前保留的计数器数量
     */
    int liveKeyCount() {
        return counters.size();
    }

    @Override
    public boolean reset(String key) {
        return counters.remove(key) != null;
    }

    @Override
    public Type getType() {
        return Type.MEMORY;
    }

    /**
     * 单个 key 的窗口计数器
     */
    private static final class Counter {

        /** 当前窗口内的请求数 */
        private final AtomicInteger count = new AtomicInteger();

        /** 当前窗口的到期时间戳（毫秒） */
        private final long expireAt;

        Counter(Duration window) {
            this.expireAt = System.currentTimeMillis() + window.toMillis();
        }

        /**
         * 计数加一
         *
         * @return 递增后的计数
         */
        int increment() {
            return count.incrementAndGet();
        }

        /**
         * 窗口是否已结束
         */
        boolean isStale() {
            return expireAt - System.currentTimeMillis() <= 0;
        }

        /**
         * 距离窗口重置的剩余时长
         */
        Duration remaining() {
            return Duration.ofMillis(Math.max(0, expireAt - System.currentTimeMillis()));
        }

    }

}
