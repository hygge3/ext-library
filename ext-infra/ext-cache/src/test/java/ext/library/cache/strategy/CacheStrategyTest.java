package ext.library.cache.strategy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

}
