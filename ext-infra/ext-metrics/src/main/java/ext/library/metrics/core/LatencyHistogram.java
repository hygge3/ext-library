package ext.library.metrics.core;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.LongAdder;

/**
 * 对数分桶延迟直方图
 * <p>
 * 不保存每个样本，只用固定边界计数。分位取命中桶的<b>上界</b>：600ms 落在 {@code (500,750]} 里报 750。
 * 宁可高估也不低估——告警要的是不漏报。
 */
final class LatencyHistogram {

    /**
     * 桶边界（毫秒），阈值附近划细，越往上越粗
     */
    static final long[] BOUNDS = {1, 2, 5, 10, 20, 30, 50, 75, 100, 150, 200, 300, 400, 500, 750, 1_000, 1_500, 2_000, 3_000, 5_000, 10_000};

    private final AtomicLongArray counts = new AtomicLongArray(BOUNDS.length + 1);

    private final LongAdder total = new LongAdder();

    void record(long millis) {
        int index = BOUNDS.length;
        for (int i = 0; i < BOUNDS.length; i++) {
            if (millis <= BOUNDS[i]) {
                index = i;
                break;
            }
        }
        counts.incrementAndGet(index);
        total.increment();
    }

    /**
     * 分位值
     *
     * @param percentile 百分位，如 0.99
     *
     * @return 命中桶的上界，超过最大边界时返回 {@link Long#MAX_VALUE}；无样本返回 0
     */
    long percentile(double percentile) {
        long samples = total.sum();
        if (samples == 0) {
            return 0;
        }
        long target = Math.max(1, (long) Math.ceil(samples * percentile));
        long cumulative = 0;
        for (int i = 0; i < counts.length(); i++) {
            cumulative += counts.get(i);
            if (cumulative >= target) {
                return i < BOUNDS.length ? BOUNDS[i] : Long.MAX_VALUE;
            }
        }
        return 0;
    }

    void reset() {
        for (int i = 0; i < counts.length(); i++) {
            counts.set(i, 0);
        }
        total.reset();
    }
}
