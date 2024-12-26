package ext.library.redis.lock;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import ext.library.redis.lock.function.ExceptionHandler;

/**
 * 状态处理器
 *
 * @param <T> 返回类型
 */
public interface StateHandler<T> {

	/**
	 * 获取锁成功，业务方法执行成功回调
	 * @param action 回调方法引用
	 * @return 状态处理器
	 */
	StateHandler<T> onSuccess(UnaryOperator<T> action);

	/**
	 * 获取锁失败回调
	 * @param action 回调方法引用
	 * @return 状态处理器
	 */
	StateHandler<T> onLockFail(Supplier<T> action);

	/**
	 * 获取锁成功，执行业务方法异常回调
	 * @param action 回调方法引用
	 * @return 状态处理器
	 */
	StateHandler<T> onException(ExceptionHandler action);

	/**
	 * 控制锁竞争失败时的重试次数
	 * @param retryCount 重试次数
	 * @return {@code StateHandler<T> }
	 */
	StateHandler<T> retryCount(int retryCount);

	/**
	 * 终态，获取锁
	 * @return result
	 */
	T lock();

}
