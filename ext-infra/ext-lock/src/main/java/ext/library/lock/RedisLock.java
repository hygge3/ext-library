package ext.library.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁注解
 * <p>
 * 在方法上标注此注解，即可对该方法加上一把基于 Redis 的分布式锁，
 * 方法执行完毕后自动释放。同一时刻只允许一个线程进入被标注的方法。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @RedisLock(key = "'order:' + #orderId", message = "订单正在处理中")
 * public Result<Void> submit(Order order) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLock {

    /**
     * 默认的锁 key 前缀
     */
    String KEY_PREFIX = "ext:lock";

    /**
     * SpEL 表达式，从上下文中提取锁的业务维度标识
     * <p>
     * 支持的上下文变量：
     * <ul>
     *   <li>方法参数名（如 #orderId, #user）</li>
     *   <li>#request - HttpServletRequest（仅 Servlet 环境）</li>
     *   <li>#p0、#p1 - 按位置索引的方法参数</li>
     * </ul>
     * 留空时按「类名 + 方法名」加锁。
     *
     * @return SpEL 表达式
     */
    String key() default "";

    /**
     * 锁的持有时间
     * <p>
     * 到期后锁会被自动释放，防止持有者宕机导致死锁。
     * 应略大于方法的最长执行耗时。
     *
     * @return 持有时间
     */
    long timeout() default 30;

    /**
     * 锁持有时间的单位
     *
     * @return 时间单位，默认为秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 获取锁的最长等待时间，小于等于 0 表示不等待，抢不到立即失败
     * <p>
     * 取值大于 0 时，超时未获取到锁将抛出 {@link LockException}。
     *
     * @return 等待时间
     */
    long waitTime() default 0;

    /**
     * 等待锁的时间单位
     *
     * @return 时间单位，默认为秒
     */
    TimeUnit waitTimeUnit() default TimeUnit.SECONDS;

    /**
     * 等待锁时自旋重试的间隔
     *
     * @return 间隔时间
     */
    long loopInterval() default 10;

    /**
     * 自旋间隔的时间单位
     *
     * @return 时间单位，默认为毫秒
     */
    TimeUnit loopIntervalUnit() default TimeUnit.MILLISECONDS;

    /**
     * 未获取到锁时的提示信息
     *
     * @return 提示信息
     */
    String message() default "操作正在处理中，请稍后重试";

}
