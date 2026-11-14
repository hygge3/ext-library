package ext.library.metrics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link LatencyHistogram} 的用例
 */
class LatencyHistogramTest {

    @Test
    void emptyHistogramReportsZero() {
        assertEquals(0, new LatencyHistogram().percentile(0.99));
    }

    @Test
    void percentileReturnsBucketUpperBound() {
        LatencyHistogram histogram = new LatencyHistogram();
        for (int i = 0; i < 98; i++) {
            histogram.record(10);
        }
        histogram.record(600);
        histogram.record(600);

        // 100 个样本里第 99 小是 600，落在 (500,750]，P99 取桶上界
        assertEquals(750, histogram.percentile(0.99));
    }

    @Test
    void smallValuesStayInNarrowBucket() {
        LatencyHistogram histogram = new LatencyHistogram();
        for (int i = 0; i < 100; i++) {
            histogram.record(12);
        }
        assertEquals(20, histogram.percentile(0.99));
    }

    @Test
    void overflowBucketReportsMaxValue() {
        LatencyHistogram histogram = new LatencyHistogram();
        for (int i = 0; i < 100; i++) {
            histogram.record(60_000);
        }
        assertEquals(Long.MAX_VALUE, histogram.percentile(0.99));
    }

    @Test
    void resetClearsSamples() {
        LatencyHistogram histogram = new LatencyHistogram();
        histogram.record(100);
        histogram.reset();
        assertEquals(0, histogram.percentile(0.99));
    }
}
