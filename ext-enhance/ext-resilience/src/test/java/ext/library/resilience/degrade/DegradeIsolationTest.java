package ext.library.resilience.degrade;

import ext.library.core.util.SpringUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.context.support.GenericApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 隔离线程池与超时：超时算一次失败，核心接口不允许配降级。
 */
@DisplayName("熔断降级：线程池隔离与超时")
class DegradeIsolationTest {

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    @BeforeEach
    void setUp() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.getBeanFactory().registerSingleton("corePool", pool);
        context.refresh();
        SpringUtil.setContext(context);
    }

    @AfterEach
    void tearDown() {
        pool.shutdownNow();
    }

    private static <T> T proxy(T target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(new DegradeAspect(new DegradeRegistry()));
        return factory.getProxy();
    }

    @Test
    @DisplayName("过池执行正常返回")
    void runOnIsolatedPool() {
        assertThat(proxy(new IsolatedService()).call(0)).isEqualTo("ok");
    }

    @Test
    @DisplayName("超时抛 IsolationTimeoutException 并计入失败")
    void timeoutCountsAsFailure() {
        IsolatedService service = proxy(new IsolatedService());

        assertThatThrownBy(() -> service.call(500))
                .isInstanceOf(IsolationTimeoutException.class)
                .hasMessageContaining("50");

        // minCalls = 2，第二次超时后熔断打开，第三次直接走降级
        assertThatThrownBy(() -> service.call(500)).isInstanceOf(IsolationTimeoutException.class);
        assertThat(service.call(500)).isEqualTo("fallback");
    }

    @Test
    @DisplayName("核心接口配降级方法是配置错误")
    void criticalMustNotDegrade() {
        assertThatThrownBy(() -> proxy(new CriticalService()).call())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("核心接口");
    }

    @Test
    @DisplayName("配了超时却没配线程池是配置错误")
    void timeoutWithoutExecutor() {
        assertThatThrownBy(() -> proxy(new NoExecutorService()).call())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("executor");
    }

    static class IsolatedService {

        @Degrade(name = "isolated", minCalls = 2, failureRate = 100, fallback = "fallback",
                executor = "corePool", timeoutMillis = 50)
        public String call(long sleepMillis) {
            sleep(sleepMillis);
            return "ok";
        }

        String fallback(long sleepMillis) {
            return "fallback";
        }
    }

    static class CriticalService {

        @Degrade(critical = true, fallback = "fallback")
        public String call() {
            return "ok";
        }

        String fallback() {
            return "fallback";
        }
    }

    static class NoExecutorService {

        @Degrade(timeoutMillis = 50)
        public String call() {
            return "ok";
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

}
