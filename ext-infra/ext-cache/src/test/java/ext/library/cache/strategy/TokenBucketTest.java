package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.util.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 令牌桶语义：容量个瞬时请求全部放行，超出拒绝；空桶后按补满速率逐个恢复。
 * <p>
 * 时间源是 {@link System#nanoTime()}，所以用毫秒级短周期测，靠 sleep 推进令牌补充。
 */
@DisplayName("令牌桶")
class TokenBucketTest {

    private static final long CAPACITY = 3;
    private static final Duration REFILL_PERIOD = Duration.ofMillis(300);

    private final CaffeineStrategy strategy = new CaffeineStrategy();

    @BeforeEach
    void setUp() {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(CacheProperties.class)).thenReturn(new CacheProperties());
        SpringUtil.setContext(context);
    }

    @AfterEach
    void tearDown() {
        SpringUtil.clearContext();
    }

    @Test
    @DisplayName("容量个突发全部放行，超出拒绝")
    void burstUntilCapacity() {
        assertThat(strategy.tryAcquire("ratelimit", "burst", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        assertThat(strategy.tryAcquire("ratelimit", "burst", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        assertThat(strategy.tryAcquire("ratelimit", "burst", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        assertThat(strategy.tryAcquire("ratelimit", "burst", 1, CAPACITY, REFILL_PERIOD).allowed()).isFalse();
    }

    @Test
    @DisplayName("等待一个令牌间隔后恢复放行")
    void refillOneToken() throws InterruptedException {
        for (int i = 0; i < CAPACITY; i++) {
            assertThat(strategy.tryAcquire("ratelimit", "refill", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        }
        assertThat(strategy.tryAcquire("ratelimit", "refill", 1, CAPACITY, REFILL_PERIOD).allowed()).isFalse();

        Thread.sleep(150);

        assertThat(strategy.tryAcquire("ratelimit", "refill", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        assertThat(strategy.tryAcquire("ratelimit", "refill", 1, CAPACITY, REFILL_PERIOD).allowed()).isFalse();
    }

    @Test
    @DisplayName("不同 key 的桶互不影响，不抢同一个桶")
    void isolatedByKey() {
        for (int i = 0; i < CAPACITY; i++) {
            assertThat(strategy.tryAcquire("ratelimit", "user-1", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        }
        assertThat(strategy.tryAcquire("ratelimit", "user-1", 1, CAPACITY, REFILL_PERIOD).allowed()).isFalse();
        assertThat(strategy.tryAcquire("ratelimit", "user-2", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
    }

    @Test
    @DisplayName("一次取多个令牌按量扣减")
    void acquireMultiplePermits() {
        assertThat(strategy.tryAcquire("ratelimit", "multi", 2, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
        assertThat(strategy.tryAcquire("ratelimit", "multi", 2, CAPACITY, REFILL_PERIOD).allowed()).isFalse();
        assertThat(strategy.tryAcquire("ratelimit", "multi", 1, CAPACITY, REFILL_PERIOD).allowed()).isTrue();
    }

}
