package ext.library.resilience.degrade;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 熔断器注册表
 * <p>
 * 同一名称的切点共享同一个熔断器；配置取首次注册时的注解值，运行期改注解不会重建熔断器。
 */
public class DegradeRegistry {

    private final ConcurrentHashMap<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    /**
     * 取得熔断器，不存在则按注解配置创建
     *
     * @param key     熔断器名称
     * @param degrade 降级注解
     *
     * @return {@link CircuitBreaker }
     */
    public CircuitBreaker get(String key, Degrade degrade) {
        return breakers.computeIfAbsent(key, k -> new CircuitBreaker(
                degrade.minCalls(),
                degrade.failureRate(),
                Duration.of(degrade.window(), degrade.timeUnit().toChronoUnit()),
                Duration.of(degrade.openDuration(), degrade.openTimeUnit().toChronoUnit())));
    }

    /**
     * 已注册的熔断器数量
     */
    public int size() {
        return breakers.size();
    }

}
