package ext.library.cache.strategy;

import ext.library.cache.properties.CacheProperties;
import ext.library.core.backend.BackendType;
import ext.library.core.token.TokenBucketResult;
import ext.library.core.util.SpringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.Type;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link CacheStrategy#narrow} 是「缓存值 → 方法返回值」的唯一出口。
 * <p>
 * 曾直接用 {@code clazz.cast(value)}：基本类型返回（{@code int}）第二次调用必抛 {@code ClassCastException}。
 */
@DisplayName("缓存值窄化")
class CacheStrategyTest {

    private final CacheStrategy strategy = new CacheStrategy() {

        @Override
        public <T> T get(String cacheName, String key, Type type) {
            return null;
        }

        @Override
        public <T> T put(String cacheName, String key, T value, Duration expireTime) {
            return value;
        }

        @Override
        public <T> T put(String cacheName, String key, T value) {
            return value;
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

    @Test
    @DisplayName("String 原样返回")
    void string() {
        assertThat((Object) strategy.narrow("abc", String.class)).isEqualTo("abc");
    }

    @Test
    @DisplayName("基本类型按包装类判定")
    void primitive() {
        assertThat((Object) strategy.narrow(5, int.class)).isEqualTo(5);
        assertThat((Object) strategy.narrow(true, boolean.class)).isEqualTo(true);
    }

    @Test
    @DisplayName("类型不符视为未命中而不是抛异常")
    void mismatch() {
        assertThat((Object) strategy.narrow("5", Integer.class)).isNull();
        assertThat((Object) strategy.narrow(5, String.class)).isNull();
    }

    @Test
    @DisplayName("值为 null 返回 null")
    void nullValue() {
        assertThat((Object) strategy.narrow(null, String.class)).isNull();
    }

    @Test
    @DisplayName("抖动只缩短不延长，且不超上限")
    void jitterShrinksOnly() {
        CacheProperties properties = new CacheProperties();
        properties.setExpireJitter(0.5);
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean(CacheProperties.class, () -> properties);
        context.refresh();
        SpringUtil.setContext(context);
        try {
            Duration ttl = Duration.ofSeconds(100);
            for (int i = 0; i < 200; i++) {
                assertThat(strategy.withJitter(ttl)).isBetween(Duration.ofSeconds(50), ttl);
            }

            assertThat(strategy.withJitter(null)).isNull();
            assertThat(strategy.withJitter(Duration.ZERO)).isZero();

            properties.setExpireJitter(0);
            assertThat(strategy.withJitter(ttl)).isEqualTo(ttl);
        } finally {
            SpringUtil.clearContext();
            context.close();
        }
    }

}
