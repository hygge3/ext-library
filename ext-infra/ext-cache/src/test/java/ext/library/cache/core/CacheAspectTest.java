package ext.library.cache.core;

import ext.library.cache.annotation.Cache;
import ext.library.cache.enums.CacheType;
import ext.library.cache.strategy.CacheStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code @Cache(PUT)} 曾忽略注解上的 {@code timeout}/{@code timeUnit}，写进去的值按默认 1 天过期。
 */
@DisplayName("缓存切面")
class CacheAspectTest {

    private final List<Duration> putExpireTimes = new ArrayList<>();

    private final CacheStrategy strategy = new CacheStrategy() {

        @Override
        public <T> T get(String cacheName, String key, Type type) {
            return null;
        }

        @Override
        public <T> T put(String cacheName, String key, T value, Duration expireTime) {
            putExpireTimes.add(expireTime);
            return value;
        }

        @Override
        public <T> T put(String cacheName, String key, T value) {
            return put(cacheName, key, value, null);
        }

        @Override
        public void evict(String cacheName, String key) {
        }

        @Override
        public void clear(String cacheName) {
        }

        @Override
        public boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime) {
            return true;
        }

    };

    static class Service {

        @Cache(cacheName = "user", key = "#id", type = CacheType.PUT, timeout = 5, timeUnit = TimeUnit.MINUTES)
        public String put(Long id) {
            return "v" + id;
        }

    }

    @Test
    @DisplayName("PUT 沿用注解声明的过期时间")
    void putHonoursDeclaredTimeout() throws Throwable {
        Method method = Service.class.getMethod("put", Long.class);
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(point.getSignature()).thenReturn(signature);
        when(point.getThis()).thenReturn(new Service());
        when(point.getArgs()).thenReturn(new Object[]{1L});
        when(point.proceed()).thenReturn("v1");

        Object result = new CacheAspect(strategy).doAround(point);

        assertThat(result).isEqualTo("v1");
        assertThat(putExpireTimes).containsExactly(Duration.ofMinutes(5));
    }

}
