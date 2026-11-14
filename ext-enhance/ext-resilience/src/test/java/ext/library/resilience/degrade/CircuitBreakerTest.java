package ext.library.resilience.degrade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 熔断状态机：样本不足不熔断、达阈值打开、到期半开只放一个探测、探测结果决定恢复还是继续熔断。
 */
@DisplayName("熔断器")
class CircuitBreakerTest {

    private static final Duration WINDOW = Duration.ofSeconds(10);
    private static final Duration OPEN_DURATION = Duration.ofSeconds(5);

    private long now;

    private CircuitBreaker breaker(int minCalls, int failureRate) {
        return new CircuitBreaker(minCalls, failureRate, WINDOW, OPEN_DURATION, () -> now);
    }

    @Test
    @DisplayName("样本不足 minCalls 不熔断")
    void notTripBelowMinCalls() {
        CircuitBreaker breaker = breaker(4, 50);
        for (int i = 0; i < 3; i++) {
            assertThat(breaker.allowRequest()).isTrue();
            breaker.onFailure();
        }
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(breaker.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("失败率达阈值立刻打开并拒绝后续请求")
    void tripAtFailureRate() {
        CircuitBreaker breaker = breaker(4, 50);
        for (int i = 0; i < 4; i++) {
            assertThat(breaker.allowRequest()).isTrue();
            breaker.onFailure();
        }
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        assertThat(breaker.allowRequest()).isFalse();
    }

    @Test
    @DisplayName("失败率未达阈值保持关闭")
    void stayClosedBelowFailureRate() {
        CircuitBreaker breaker = breaker(4, 60);
        breaker.allowRequest();
        breaker.onSuccess();
        breaker.allowRequest();
        breaker.onSuccess();
        breaker.allowRequest();
        breaker.onFailure();
        breaker.allowRequest();
        breaker.onFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("熔断到期只放一个探测请求，探测成功恢复关闭")
    void halfOpenThenRecover() {
        CircuitBreaker breaker = breaker(2, 50);
        breaker.allowRequest();
        breaker.onFailure();
        breaker.allowRequest();
        breaker.onFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        now += OPEN_DURATION.toNanos() - 1;
        assertThat(breaker.allowRequest()).isFalse();

        now += 1;
        assertThat(breaker.allowRequest()).isTrue();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.HALF_OPEN);
        // 探测请求在途时其它请求继续走降级
        assertThat(breaker.allowRequest()).isFalse();

        breaker.onSuccess();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(breaker.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("探测失败重新熔断并重新计时")
    void halfOpenThenTripAgain() {
        CircuitBreaker breaker = breaker(2, 50);
        breaker.allowRequest();
        breaker.onFailure();
        breaker.allowRequest();
        breaker.onFailure();

        now += OPEN_DURATION.toNanos();
        assertThat(breaker.allowRequest()).isTrue();
        breaker.onFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        now += OPEN_DURATION.toNanos() - 1;
        assertThat(breaker.allowRequest()).isFalse();
        now += 1;
        assertThat(breaker.allowRequest()).isTrue();
    }

    @Test
    @DisplayName("统计窗口过期后计数清零，旧失败不再拖累")
    void windowResets() {
        CircuitBreaker breaker = breaker(2, 100);
        breaker.allowRequest();
        breaker.onFailure();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);

        now += WINDOW.toNanos();
        breaker.allowRequest();
        breaker.onSuccess();
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        assertThat(breaker.getCalls()).isEqualTo(1);
    }

    @Test
    @DisplayName("非法配置直接拒绝")
    void rejectInvalidConfig() {
        assertThatThrownBy(() -> breaker(0, 50)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> breaker(1, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> breaker(1, 101)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("熔断期间的在途失败不延长熔断")
    void inFlightFailureDoesNotExtendOpen() {
        CircuitBreaker breaker = breaker(4, 50);
        for (int i = 0; i < 4; i++) {
            assertThat(breaker.allowRequest()).isTrue();
            breaker.onFailure();
        }
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        // 打开前就发出去的请求现在才失败
        now = OPEN_DURATION.toNanos() - 1;
        breaker.onFailure();

        now = OPEN_DURATION.toNanos();
        assertThat(breaker.allowRequest()).as("熔断应按首次打开的时刻到期，不被旧失败往后推").isTrue();
    }

    @Test
    @DisplayName("探测卡死超过 openDuration 后允许下一个探测")
    void stuckProbeIsReleased() {
        CircuitBreaker breaker = breaker(4, 50);
        for (int i = 0; i < 4; i++) {
            breaker.allowRequest();
            breaker.onFailure();
        }

        now = OPEN_DURATION.toNanos();
        assertThat(breaker.allowRequest()).isTrue();
        assertThat(breaker.allowRequest()).isFalse();

        now = OPEN_DURATION.toNanos() * 2;
        assertThat(breaker.allowRequest()).as("探测线程卡住不能让降级永久生效").isTrue();
    }

}
