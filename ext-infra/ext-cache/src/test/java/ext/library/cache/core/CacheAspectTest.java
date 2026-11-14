package ext.library.cache.core;

import ext.library.cache.annotation.Cache;
import ext.library.cache.enums.CacheType;
import ext.library.cache.properties.CacheProperties;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.backend.BackendType;
import ext.library.core.token.TokenBucketResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 切面要覆盖的三件事：注解声明的 TTL、空值负缓存、同 key 并发回源合并。
 */
@DisplayName("缓存切面")
class CacheAspectTest {

    private final Map<String, Object> store = new ConcurrentHashMap<>();

    private final List<Duration> putExpireTimes = new ArrayList<>();

    private final CacheStrategy strategy = new CacheStrategy() {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(String cacheName, String key, Type type) {
            return (T) store.get(cacheName + ":" + key);
        }

        @Override
        public <T> T put(String cacheName, String key, T value, Duration expireTime) {
            putExpireTimes.add(expireTime);
            store.put(cacheName + ":" + key, value);
            return value;
        }

        @Override
        public <T> T put(String cacheName, String key, T value) {
            return put(cacheName, key, value, null);
        }

        @Override
        public void evict(String cacheName, String key) {
            store.remove(cacheName + ":" + key);
        }

        @Override
        public void clear(String cacheName) {
            store.clear();
        }

        @Override
        public boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime) {
            return store.putIfAbsent(cacheName + ":" + key, value) == null;
        }

        @Override
        public long increment(String cacheName, String key, long delta, Duration window) {
            return delta;
        }

        @Override
        public TokenBucketResult tryAcquire(String cacheName, String key, long permits, long capacity,
                                            Duration refillPeriod) {
            return new TokenBucketResult(true, capacity - permits, 0);
        }

        @Override
        public BackendType backendType() {
            return BackendType.MEMORY;
        }

    };

    private final CacheAspect aspect = new CacheAspect(strategy, new CacheProperties());

    @FunctionalInterface
    private interface Loader {

        Object load() throws Throwable;

    }

    static class Service {

        @Cache(cacheName = "user", key = "#id", type = CacheType.PUT, timeout = 5, timeUnit = TimeUnit.MINUTES)
        public String put(Long id) {
            return "v" + id;
        }

        @Cache(cacheName = "order", key = "#id", timeout = 5, timeUnit = TimeUnit.MINUTES, cacheNull = true, nullTimeout = 10)
        public String find(Long id) {
            return null;
        }

        @Cache(cacheName = "order", key = "#id", type = CacheType.DELETE)
        public void remove(Long id) {
        }

    }

    @Test
    @DisplayName("PUT 沿用注解声明的过期时间")
    void putHonoursDeclaredTimeout() throws Throwable {
        Object result = aspect.doAround(point("put", Long.class, 1L, () -> "v1"));

        assertThat(result).isEqualTo("v1");
        assertThat(putExpireTimes).containsExactly(Duration.ofMinutes(5));
    }

    @Test
    @DisplayName("空值按 nullTimeout 写负缓存，第二次不再回源")
    void nullResultCachedBriefly() throws Throwable {
        AtomicInteger loads = new AtomicInteger();
        ProceedingJoinPoint point = point("find", Long.class, 2L, () -> {
            loads.incrementAndGet();
            return null;
        });

        assertThat(aspect.doAround(point)).isNull();
        assertThat(aspect.doAround(point)).isNull();

        assertThat(loads).hasValue(1);
        assertThat(store).containsEntry("order:2:null", Boolean.TRUE);
        assertThat(putExpireTimes).containsExactly(Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("DELETE 连负缓存标记一起删")
    void deleteAlsoEvictsNullMarker() throws Throwable {
        store.put("order:3:null", Boolean.TRUE);

        aspect.doAround(point("remove", Long.class, 3L, () -> null));

        assertThat(store).doesNotContainKey("order:3:null");
    }

    @Test
    @DisplayName("同 key 并发回源只打一次数据库")
    void concurrentLoadCollapsesToOne() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        CountDownLatch loading = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Loader loader = () -> {
            loads.incrementAndGet();
            loading.countDown();
            release.await(2, TimeUnit.SECONDS);
            return "v4";
        };
        ProceedingJoinPoint point = point("find", Long.class, 4L, loader);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Object> first = pool.submit(() -> call(point));
            assertThat(loading.await(2, TimeUnit.SECONDS)).isTrue();
            Future<Object> second = pool.submit(() -> call(point));
            // 第二个请求已在等首次加载的结果，等它进到 inflight 再放行
            Thread.sleep(200);
            release.countDown();

            assertThat(first.get(2, TimeUnit.SECONDS)).isEqualTo("v4");
            assertThat(second.get(2, TimeUnit.SECONDS)).isEqualTo("v4");
            assertThat(loads).hasValue(1);
        } finally {
            pool.shutdownNow();
        }
    }

    private Object call(ProceedingJoinPoint point) {
        try {
            return aspect.doAround(point);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private ProceedingJoinPoint point(String name, Class<?> paramType, Object arg, Loader loader) {
        try {
            Method method = Service.class.getMethod(name, paramType);
            ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
            MethodSignature signature = mock(MethodSignature.class);
            when(signature.getMethod()).thenReturn(method);
            when(point.getSignature()).thenReturn(signature);
            when(point.getThis()).thenReturn(new Service());
            when(point.getArgs()).thenReturn(new Object[]{arg});
            when(point.proceed()).thenAnswer(invocation -> loader.load());
            return point;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

}
