package ext.library.redis.cache.operation.function;

/**
 * 结果方法
 */
@FunctionalInterface
public interface ResultMethod<T> {

	/**
	 * 执行并返回一个结果
	 * @return result
	 */
	T run();

}
