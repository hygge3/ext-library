package ext.library.resilience.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 令牌桶限流
 * <p>
 * 桶容量 {@link #limit()} 是允许的瞬时突发量，{@link #window()} 是把空桶补满所需时长，
 * 因此平滑速率 = {@code limit / window}。例：{@code limit = 10, window = 1, timeUnit = SECONDS}
 * 表示平均 10 rps，空闲后可以一次打满 10 个请求。
 * <p>
 * 计数落点跟随 {@code ext.cache.storage}，多实例共享同一个桶。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 限流 key 前缀，用于区分不同业务域
     */
    String prefix() default "";

    /**
     * 限流维度的 SpEL 表达式，如 {@code "#userId"}、{@code "#request.remoteAddr"}；留空则按「类名#方法名」整体限流
     */
    String key() default "";

    /**
     * 桶容量，即允许的瞬时突发量
     */
    long limit() default 10;

    /**
     * 每次调用消耗的令牌数，不得超过 {@link #limit()}
     */
    long permits() default 1;

    /**
     * 把空桶补满所需的时长数值，配合 {@link #timeUnit()}
     */
    long window() default 1;

    /**
     * 补满周期的单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 超出限制时的提示信息
     */
    String message() default "";
}
