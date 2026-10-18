package ext.library.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流注解
 * <p>
 * 在方法上标注此注解，即可按指定的 key 与阈值对该方法进行限流。超过阈值时抛出
 * {@link RateLimitException}，由业务侧的全局异常处理器转成对应响应。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @RateLimit(key = "#request.getRemoteAddr()", limit = 100, window = 1, timeUnit = TimeUnit.MINUTES)
 * public Result<Void> sendCode(String phone) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 默认的限流 key 前缀
     */
    String KEY_PREFIX = "ext:ratelimit";

    /**
     * 限流标识前缀，可用于区分服务和业务，防止 key 冲突
     *
     * @return 业务标识前缀
     */
    String prefix() default KEY_PREFIX;

    /**
     * SpEL 表达式，从上下文中提取限流维度的唯一标识
     * <p>
     * 支持的上下文变量：
     * <ul>
     *   <li>方法参数名（如 #phone, #order）</li>
     *   <li>#request - HttpServletRequest（仅 Servlet 环境）</li>
     *   <li>#p0、#p1 - 按位置索引的方法参数</li>
     * </ul>
     * 留空时按「类名 + 方法名」整体限流。
     *
     * @return SpEL 表达式
     */
    String key() default "";

    /**
     * 时间窗口内允许的最大请求数
     *
     * @return 阈值
     */
    int limit() default 100;

    /**
     * 限流时间窗口
     *
     * @return 窗口大小
     */
    long window() default 1;

    /**
     * 时间窗口单位
     *
     * @return 时间单位，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 被限流时的提示信息
     *
     * @return 提示信息
     */
    String message() default "请求过于频繁，请稍后重试";

}
