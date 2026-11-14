package ext.library.idempotent;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交
 * <p>
 * 与 {@link Idempotent} 的区别：幂等键由业务数据（订单号、表单 ID）推导，目的是「同一笔业务只生效一次」；
 * 本注解的键由「用户 + 接口 + 参数指纹」推导，目的是「同一个人对同一接口的连点只放行一次」，
 * 业务方不用写表达式。要防的是手抖双击、网关重投，不是数据重复。
 * <p>
 * 键写入 {@link ext.library.cache.strategy.CacheStrategy#putIfAbsent}，与 {@link Idempotent} 同一套原子保证。
 *
 * @see RepeatSubmitKeyGenerator
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /**
     * 键前缀
     */
    String prefix() default "repeat";

    /**
     * 控制时长，默认 3 秒——够挡住连点，又不至于拦住用户看完提示后的正常重试
     */
    int duration() default 3;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 重复提交时的提示信息
     */
    String message() default "请勿重复提交";

    /**
     * 键写入的缓存名
     */
    String cacheName() default "repeat-submit";

    /**
     * 额外区分维度（SpEL），默认为空
     * <p>
     * 参数指纹已包含全部入参；只有需要按「不参与入参」的维度区分时才填，例如从 session 取的用户 ID。
     */
    String uniqueExpression() default "";

}
