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

    /** 计数表：key → 窗口计数器 */
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult increment(String key, int limit, Duration window) {
        Counter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired(window)) {
                return new Counter();
            }
            return existing;
        });
        int count = counter.increment();
        counter.expire(window);
        return new RateLimitResult(count <= limit, count, Math.max(0, limit - count), limit, counter.remaining());
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
        private volatile long expireAt;

        /**
         * 计数加一
         *
         * @return 递增后的计数
         */
        int increment() {
            return count.incrementAndGet();
        }

        /**
         * 续期整个窗口：仅当上一轮窗口已结束、或剩余时长超过整个窗口长度时重置到期时间
         *
         * @param window 窗口长度
         */
        void expire(Duration window) {
            if (isExpired(window)) {
                expireAt = System.currentTimeMillis() + window.toMillis();
            }
        }

        /**
         * 窗口是否已过期
         */
        boolean isExpired(Duration window) {
            long remaining = expireAt - System.currentTimeMillis();
            return remaining <= 0 || remaining > window.toMillis();
        }

        /**
         * 距离窗口重置的剩余时长
         */
        Duration remaining() {
            return Duration.ofMillis(Math.max(0, expireAt - System.currentTimeMillis()));
        }

    }

}
