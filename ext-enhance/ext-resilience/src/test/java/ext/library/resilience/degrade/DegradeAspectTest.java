package ext.library.resilience.degrade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 降级只在熔断打开时生效：原方法自己抛的异常照原样上抛，不能被降级吞掉。
 */
@DisplayName("熔断降级切面")
class DegradeAspectTest {

    private static final int MIN_CALLS = 2;

    private final CallbackService raw = new CallbackService();

    private final CallbackService service = proxy(raw);

    private static <T> T proxy(T target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new DegradeAspect(new DegradeRegistry()));
        return factory.getProxy();
    }

    @Test
    @DisplayName("正常调用直接走原方法")
    void passThroughWhenClosed() {
        assertThat(service.call(false)).isEqualTo("ok");
        assertThat(raw.invocations).isEqualTo(1);
    }

    @Test
    @DisplayName("熔断前的失败照原样抛给调用方")
    void propagateFailureBeforeTrip() {
        assertThatThrownBy(() -> service.call(true)).isInstanceOf(IllegalStateException.class);
        assertThat(raw.invocations).isEqualTo(1);
    }

    @Test
    @DisplayName("熔断后走降级方法且不再调用原方法")
    void fallbackWhenOpen() {
        assertThatThrownBy(() -> service.call(true)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> service.call(true)).isInstanceOf(IllegalStateException.class);
        assertThat(raw.invocations).isEqualTo(MIN_CALLS);

        assertThat(service.call(true)).isEqualTo("fallback");
        assertThat(raw.invocations).isEqualTo(MIN_CALLS);
    }

    @Test
    @DisplayName("未配置降级方法时抛 CircuitOpenException")
    void circuitOpenWithoutFallback() {
        NoFallbackService target = proxy(new NoFallbackService());
        assertThatThrownBy(target::call).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(target::call).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(target::call).isInstanceOf(CircuitOpenException.class);
    }

    static class CallbackService {

        int invocations;

        @Degrade(minCalls = MIN_CALLS, failureRate = 100, fallback = "fallback")
        public String call(boolean fail) {
            invocations++;
            if (fail) {
                throw new IllegalStateException("下游不可用");
            }
            return "ok";
        }

        String fallback(boolean fail) {
            return "fallback";
        }
    }

    static class NoFallbackService {

        @Degrade(minCalls = MIN_CALLS, failureRate = 100)
        public String call() {
            throw new IllegalStateException("下游不可用");
        }
    }

}
