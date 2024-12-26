package ext.library.redis.lock;

import java.util.concurrent.TimeUnit;

import ext.library.redis.config.properties.RedisPropertiesHolder;
import ext.library.tool.holder.function.CheckedSupplier;

/**
 * 锁住的方法
 *
 * @param <T> 返回类型
 */
@FunctionalInterface
public interface Action<T> {

	/**
	 * 执行方法，采用配置文件中的默认过期时间
	 * @param lockKey 待锁定的 key
	 * @param supplier 执行方法
	 * @return 状态处理器
	 */
	default StateHandler<T> action(String lockKey, CheckedSupplier<T> supplier) {
		return action(lockKey, RedisPropertiesHolder.defaultLockTimeout(), TimeUnit.SECONDS, supplier);
	}

	/**
	 * 执行方法
	 * @param lockKey 待锁定的 key
	 * @param timeout 锁定过期时间，默认单位秒
	 * @param supplier 执行方法
	 * @return 状态处理器
	 */
	default StateHandler<T> action(String lockKey, long timeout, CheckedSupplier<T> supplier) {
		return action(lockKey, timeout, TimeUnit.SECONDS, supplier);
	}

	/**
	 * 执行方法
	 * @param lockKey 待锁定的 key
	 * @param timeout 锁定过期时间
	 * @param timeUnit 锁定过期时间单位
	 * @param supplier 执行方法
	 * @return 状态处理器
	 */
	StateHandler<T> action(String lockKey, long timeout, TimeUnit timeUnit, CheckedSupplier<T> supplier);

}
