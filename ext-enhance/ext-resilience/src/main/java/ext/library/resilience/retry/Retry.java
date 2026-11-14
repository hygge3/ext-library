package ext.library.resilience.retry;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 失败重试（指数退避 + 抖动）
 * <p>
 * 只允许用在幂等接口上：方法或所在类必须带 {@code @Idempotent} / {@code @RepeatSubmit}，
 * 否则首次调用直接报配置错。非幂等接口重试等于把同一笔操作做多遍。
 * <p>
 * 重试会放大流量，所以默认值保守：最多 3 次、首退避 100ms、每次乘 2、退避时长带 50% 抖动，
 * 让同一时刻失败的一批调用错开重试，而不是齐步走成一个新的尖峰。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

    /**
     * 最多尝试次数（含首次）
     */
    int maxAttempts() default 3;

    /**
     * 首次退避毫秒数
     */
    long initialDelayMillis() default 100;

    /**
     * 退避时长倍数，每次失败后乘以此值
     */
    double multiplier() default 2.0;

    /**
     * 抖动比例：0 表示固定间隔，1 表示在 {@code [0, delay]} 内随机，默认 0.5 即 {@code [delay/2, delay]}
     */
    double jitter() default 0.5;

    /**
     * 触发重试的异常类型
     */
    Class<? extends Throwable>[] retryOn() default {RuntimeException.class};

    /**
     * 确认该接口幂等，跳过幂等注解校验
     * <p>
     * 只给没法标注解的场景（如第三方接口本身幂等）用，别为了让它跑起来而无脑打开。
     */
    boolean assumeIdempotent() default false;
}
