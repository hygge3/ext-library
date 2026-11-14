package ext.library.resilience.retry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 重试只认幂等接口，退避按指数放大。
 */
@DisplayName("重试切面")
class RetryAspectTest {

    private static <T> T proxy(T target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new RetryAspect());
        return factory.getProxy();
    }

    @Test
    @DisplayName("失败次数内重试直到成功")
    void retryUntilSuccess() {
        FlakyService raw = new FlakyService(2);
        FlakyService service = proxy(raw);

        assertThat(service.call()).isEqualTo("ok");
        assertThat(raw.invocations).isEqualTo(3);
    }

    @Test
    @DisplayName("超过次数后抛原异常")
    void giveUpAfterMaxAttempts() {
        FlakyService raw = new FlakyService(Integer.MAX_VALUE);
        FlakyService service = proxy(raw);

        assertThatThrownBy(service::call).isInstanceOf(IllegalStateException.class);
        assertThat(raw.invocations).isEqualTo(3);
    }

    @Test
    @DisplayName("不在 retryOn 名单里的异常不重试")
    void skipUnlistedException() {
        IoService raw = new IoService();
        IoService service = proxy(raw);

        assertThatThrownBy(service::call).isInstanceOf(IOException.class);
        assertThat(raw.invocations).isEqualTo(1);
    }

    @Test
    @DisplayName("非幂等接口不允许重试")
    void rejectNonIdempotent() {
        assertThatThrownBy(() -> proxy(new PlainService()).call())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("@Idempotent");
    }

    @Test
    @DisplayName("显式声明幂等可跳过校验")
    void assumeIdempotent() {
        AssumedService raw = new AssumedService();
        AssumedService service = proxy(raw);

        assertThat(service.call()).isEqualTo("ok");
        assertThat(raw.invocations).isEqualTo(2);
    }

    static class FlakyService {

        private final int failures;

        int invocations;

        FlakyService(int failures) {
            this.failures = failures;
        }

        @Retry(initialDelayMillis = 1, multiplier = 2, jitter = 0, assumeIdempotent = true)
        public String call() {
            invocations++;
            if (invocations <= failures) {
                throw new IllegalStateException("下游抖动");
            }
            return "ok";
        }
    }

    static class IoService {

        int invocations;

        @Retry(initialDelayMillis = 1, assumeIdempotent = true)
        public String call() throws IOException {
            invocations++;
            throw new IOException("网络故障");
        }
    }

    static class PlainService {

        @Retry
        public String call() {
            return "ok";
        }
    }

    static class AssumedService {

        int invocations;

        @Retry(initialDelayMillis = 1, assumeIdempotent = true)
        public String call() {
            invocations++;
            if (invocations == 1) {
                throw new IllegalStateException("下游抖动");
            }
            return "ok";
        }
    }

}
