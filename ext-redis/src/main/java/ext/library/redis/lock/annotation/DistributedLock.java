package ext.library.redis.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

	/**
	 * 保证业务接口的 key 的唯一性，否则失去了分布式锁的意义 锁 key 支持使用 spEl 表达式
	 */
	String key();

	/**
	 * 保证业务接口的 key 的唯一性，否则失去了分布式锁的意义 锁 key 前缀
	 */
	String keyPrefix() default "";

	/**
	 * 获取分布式锁超时失败时间，默认 10 秒
	 * <p>
	 * 例如 5s 五秒，6m 六分钟，7h 七小时，8d 八天
	 */
	String tryAcquireTimeout() default "10s";

	/**
	 * 获取分布式锁超时提示
	 */
	String acquireTimeoutMessage() default "";

	/**
	 * 加锁的时间，超过这个时间后锁便自动解锁
	 */
	long lockTime() default 30;

	/**
	 * tryTime 和 lockTime 的时间单位
	 */
	TimeUnit unit() default TimeUnit.SECONDS;

}
