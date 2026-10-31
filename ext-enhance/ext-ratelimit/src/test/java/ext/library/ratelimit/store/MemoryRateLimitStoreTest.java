package ext.library.ratelimit.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("内存限流存储")
class MemoryRateLimitStoreTest {

    private final MemoryRateLimitStore store = new MemoryRateLimitStore();

    @Test
    @DisplayName("超过阈值后拒绝，窗口结束后重新放行")
    void windowResets() throws InterruptedException {
        Duration window = Duration.ofMillis(50);

        assertThat(store.increment("k", 2, window).allowed()).isTrue();
        assertThat(store.increment("k", 2, window).allowed()).isTrue();
        assertThat(store.increment("k", 2, window).allowed()).isFalse();

        Thread.sleep(80);

        assertThat(store.increment("k", 2, window).allowed()).isTrue();
    }

    @Test
    @DisplayName("过期计数器会被清掉，不会只增不减")
    void expiredCountersAreReclaimed() throws InterruptedException {
        Duration window = Duration.ofMillis(20);
        for (int i = 0; i < 100; i++) {
            store.increment("k" + i, 1, window);
        }
        assertThat(store.liveKeyCount()).isEqualTo(100);

        Thread.sleep(40);
        store.sweepExpired();

        assertThat(store.liveKeyCount()).isZero();
    }

}
