package ext.library.cache.strategy;

import ext.library.cache.enums.CacheStorage;
import ext.library.cache.properties.CacheProperties;
import ext.library.core.backend.BackendType;
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
 * {@link CacheStrategy#increment} 是限流的计数原语：首次计数必须把窗口写进去，窗口过期后必须从 delta 重新起算。
 * <p>
 * 键前缀由 {@code CacheProperties} 提供，所以这里给 {@code SpringUtil} 挂一个只应答 {@code getBean} 的 mock。
 */
@DisplayName("计数原语与选型默认值")
class CaffeineStrategyIncrementTest {

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
    @DisplayName("同一窗口内累加")
    void accumulate() {
        Duration window = Duration.ofMinutes(1);

        assertThat(strategy.increment("ratelimit", "/login", 1, window)).isEqualTo(1L);
        assertThat(strategy.increment("ratelimit", "/login", 1, window)).isEqualTo(2L);
        assertThat(strategy.increment("ratelimit", "/login", 3, window)).isEqualTo(5L);
        // 不同键互不影响
        assertThat(strategy.increment("ratelimit", "/register", 1, window)).isEqualTo(1L);
    }

    @Test
    @DisplayName("窗口过期后从 delta 重新起算")
    void windowExpired() throws InterruptedException {
        Duration window = Duration.ofMillis(50);

        assertThat(strategy.increment("ratelimit", "/login", 2, window)).isEqualTo(2L);
        assertThat(strategy.increment("ratelimit", "/login", 2, window)).isEqualTo(4L);

        Thread.sleep(60);

        assertThat(strategy.increment("ratelimit", "/login", 2, window)).isEqualTo(2L);
    }

    @Test
    @DisplayName("本地缓存后端为 MEMORY")
    void backendType() {
        assertThat(strategy.backendType()).isEqualTo(BackendType.MEMORY);
    }

    @Test
    @DisplayName("选型默认自动检测，且只有一个选型键")
    void defaultStorage() {
        assertThat(new CacheProperties().getCacheStorage()).isEqualTo(CacheStorage.AUTO);
    }

}
