package ext.library.resilience.ratelimit;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.token.TokenBucketResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 被限流时抛 429 并带上 Retry-After，而不是落到 500。
 */
@DisplayName("限流切面")
class RateLimitAspectTest {

    private final CacheStrategy cache = mock(CacheStrategy.class);

    private final Api raw = new Api();

    private final Api api = proxy(raw);

    private Api proxy(Api target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new RateLimitAspect(cache, new RateLimitKeyGenerator.Default(), 80, null));
        return factory.getProxy();
    }

    private void stub(TokenBucketResult result) {
        when(cache.distributed()).thenReturn(cache);
        when(cache.tryAcquire(anyString(), anyString(), anyLong(), anyLong(), any())).thenReturn(result);
    }

    @Test
    @DisplayName("桶满时正常放行")
    void passThroughWhenAllowed() {
        stub(new TokenBucketResult(true, 9, 0));

        assertThat(api.call()).isEqualTo("ok");
        assertThat(raw.invocations).isEqualTo(1);
    }

    @Test
    @DisplayName("被拒时抛 429，Retry-After 取后端给出的等待时间（向上取整到秒）")
    void rejectWithRetryAfter() {
        stub(new TokenBucketResult(false, 0, 1500));

        assertThatThrownBy(api::call).isInstanceOfSatisfying(RateLimitException.class, e -> {
            assertThat(e.status()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(e.getRetryAfterSeconds()).isEqualTo(2);
            assertThat(e.headers()).containsEntry("Retry-After", "2");
        });
        assertThat(raw.invocations).isZero();
    }

    static class Api {

        int invocations;

        @RateLimit(limit = 10, window = 1)
        public String call() {
            invocations++;
            return "ok";
        }
    }

}
