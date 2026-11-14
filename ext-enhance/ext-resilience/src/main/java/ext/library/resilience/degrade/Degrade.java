package ext.library.resilience.degrade;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 熔断降级
 * <p>
 * 统计窗口内失败率达到阈值就打开熔断，随后 {@link #openDuration()} 内不再调用原方法、直接走降级方法；
 * 到期进入半开状态放一个探测请求，成功恢复、失败继续熔断。
 * <p>
 * 熔断器是进程内状态，各实例独立统计——不做分布式熔断：一次网络抖动打穿所有实例才是常态，
 * 全局一致只会让故障传播更快。
 * <p>
 * 核心与非核心接口的区别就是这三件事：核心接口配 {@code critical = true} 且不配
 * {@link #fallback()}（熔断即快速失败，不拿旧数据糊弄主流程）、下沉到自己的线程池
 * （{@link #executor()}）、定更短的超时（{@link #timeoutMillis()}）；非核心接口配降级方法
 * 返回缓存里的兜底数据。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Degrade {

    /**
     * 熔断器名称；留空则按「类名#方法名」，同名方法共享同一个熔断器与配置（先注册者生效）
     */
    String name() default "";

    /**
     * 统计窗口内最少调用次数，样本不足不熔断，避免冷启动时一次失败就打开
     */
    int minCalls() default 20;

    /**
     * 失败率百分比阈值（1-100）
     */
    int failureRate() default 50;

    /**
     * 统计窗口大小数值，配合 {@link #timeUnit()}
     */
    long window() default 10;

    /**
     * 统计窗口单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 熔断持续时间，到期进入半开状态，数值配合 {@link #openTimeUnit()}
     */
    long openDuration() default 30;

    /**
     * 熔断持续时间单位
     */
    TimeUnit openTimeUnit() default TimeUnit.SECONDS;

    /**
     * 降级方法名，须是本 bean 内的方法，参数表同原方法或为空参；留空则熔断时抛 {@link CircuitOpenException}
     * <p>
     * 降级方法应该返回缓存里的兜底数据（可直接用 {@code CacheStrategy} 读），不能再去调已经出问题的下游。
     */
    String fallback() default "";

    /**
     * 原方法在哪个线程池里执行（bean 名称）；留空则直接用调用线程
     * <p>
     * 给核心接口配独立线程池：非核心接口再慢也只是把它们的线程占满，不会拖垮核心链路。
     * 线程池 bean 需为 {@link java.util.concurrent.Executor}，大小与拒绝策略由业务自己定。
     */
    String executor() default "";

    /**
     * 超时毫秒数，超时即取消执行并计为一次失败；0 表示不限
     * <p>
     * 必须在配了 {@link #executor()} 时才有意义：当前线程上没法中断一个同步调用。
     * 阶梯化超时就靠这里：核心链路 200ms，边缘接口 2000ms。
     */
    long timeoutMillis() default 0;

    /**
     * 核心接口：禁止配降级方法，熔断时只快速失败
     * <p>
     * 主流程拿到过期的兜底数据比直接报错更危险，所以两者同时出现直接在首次调用时报配置错。
     */
    boolean critical() default false;
}
