package ext.library.idempotent;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.tool.exception.ExtException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 异常必须释放幂等键，否则用户在整个时间窗内都被判为重复提交
 */
@DisplayName("幂等切面")
class IdempotentAspectTest {

    private static final String KEY = "idem-key";

    private static final String CACHE = "idempotent";

    static class Service {

        @Idempotent
        public String plain() {
            return "ok";
        }

        @Idempotent(removeKeyWhenFinished = true)
        public String cleanup() {
            return "ok";
        }
    }

    private final CacheStrategy cacheStrategy = mock(CacheStrategy.class);

    private final KeyGenerator keyGenerator = mock(KeyGenerator.class);

    private final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

    private final IdempotentAspect aspect = new IdempotentAspect(cacheStrategy, keyGenerator, null);

    private Idempotent annotation(String methodName) throws Exception {
        return Service.class.getMethod(methodName).getAnnotation(Idempotent.class);
    }

    private void givenKeyAbsent(boolean saved) {
        when(keyGenerator.generate(any(), any())).thenReturn(KEY);
        when(cacheStrategy.putIfAbsent(eq(CACHE), eq(KEY), any(), any(Duration.class))).thenReturn(saved);
    }

    @Test
    @DisplayName("业务异常时释放键并原样抛出")
    void releasesKeyOnFailure() throws Throwable {
        givenKeyAbsent(true);
        IllegalStateException failure = new IllegalStateException("下游超时");
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> aspect.around(joinPoint, annotation("plain"))).isSameAs(failure);
        verify(cacheStrategy).evict(CACHE, KEY);
    }

    @Test
    @DisplayName("释放键失败不掩盖业务异常")
    void evictFailureKeepsOriginalException() throws Throwable {
        givenKeyAbsent(true);
        IllegalStateException failure = new IllegalStateException("下游超时");
        when(joinPoint.proceed()).thenThrow(failure);
        doThrow(new IllegalStateException("缓存抖动")).when(cacheStrategy).evict(anyString(), anyString());

        assertThatThrownBy(() -> aspect.around(joinPoint, annotation("plain"))).isSameAs(failure);
    }

    @Test
    @DisplayName("未开启完成清理时不释放键")
    void keepsKeyOnSuccess() throws Throwable {
        givenKeyAbsent(true);
        when(joinPoint.proceed()).thenReturn("ok");

        assertThat(aspect.around(joinPoint, annotation("plain"))).isEqualTo("ok");
        verify(cacheStrategy, never()).evict(anyString(), anyString());
    }

    @Test
    @DisplayName("开启完成清理时释放键")
    void releasesKeyOnSuccessWhenConfigured() throws Throwable {
        givenKeyAbsent(true);
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.around(joinPoint, annotation("cleanup"));
        verify(cacheStrategy).evict(CACHE, KEY);
    }

    @Test
    @DisplayName("键已被占用时拒绝且不执行业务")
    void rejectsDuplicate() throws Throwable {
        givenKeyAbsent(false);

        assertThatThrownBy(() -> aspect.around(joinPoint, annotation("plain")))
                .isInstanceOf(ExtException.class)
                .hasMessageContaining("重复请求");
        verify(joinPoint, never()).proceed();
    }
}
