package ext.library.idempotent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 幂等控制注解
 * <p>
 * 通过在方法上标注此注解，可以实现接口幂等性控制，防止重复提交。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Idempotent(uniqueExpression = "#order.orderNo", duration = 30)
 * public void createOrder(Order order) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 默认的幂等 Key 前缀
     */
    String KEY_PREFIX = "ext:idempotent";

    /**
     * 幂等标识的前缀，可用于区分服务和业务，防止 key 冲突。
     * <p>
     * 完整的幂等标识 = {prefix}:{uniqueExpression.value}
     *
     * @return 业务标识前缀
     */
    String prefix() default KEY_PREFIX;

    /**
     * SpEL 表达式，从上下文中提取幂等的唯一性标识。
     * <p>
     * 支持的上下文变量：
     * <ul>
     *   <li>方法参数名（如 #order, #id）</li>
     *   <li>#request - HttpServletRequest（仅 Servlet 环境）</li>
     * </ul>
     *
     * @return SpEL 表达式
     */
    String uniqueExpression() default "";

    /**
     * 幂等的控制时长，必须大于业务的处理耗时。
     * <p>
     * 其值为幂等 key 的标记时长，超过标记时间，幂等 key 可再次使用。
     *
     * @return 标记时长，默认 10 分钟
     */
    long duration() default 10 * 60;

    /**
     * 控制时长单位
     *
     * @return 时间单位，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 重复请求时的提示信息
     *
     * @return 提示信息
     */
    String message() default "重复请求，请稍后重试";

    /**
     * 是否在业务完成后立刻清除幂等 key
     *
     * @return true: 立刻清除 false: 不处理
     */
    boolean removeKeyWhenFinished() default false;

    /**
     * 是否在业务执行异常时立刻清除幂等 key
     *
     * @return true: 立刻清除 false: 不处理
     */
    boolean removeKeyWhenError() default false;

}
