package ext.library.redis.cache.operation;

import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存操作
 */
public class CachedOps extends AbstractCacheOps {

	/**
	 * 数据类型
	 */
	private final Type returnType;

	/**
	 * 缓存分布式锁的 key
	 */
	private final String lockKey;

	/**
	 * 从 Redis 中获取缓存数据的操作
	 */
	private final Supplier<String> cacheQuery;

	/**
	 * 向缓存写入数据
	 */
	private final Consumer<Object> cachePut;

	/**
	 * 在 Redis 中锁竞争失败时的重试次数
	 */
	private final int retryCount;

	/**
	 * 基本构造函数
	 * @param joinPoint 织入方法
	 * @param lockKey 分布式锁 key
	 * @param cacheQuery 查询缓存函数
	 * @param cachePut 更新缓存函数
	 * @param returnType 返回数据类型
	 * @param retryCount 锁竞争失败时的重试次数
	 */
	public CachedOps(ProceedingJoinPoint joinPoint, String lockKey, Supplier<String> cacheQuery,
			Consumer<Object> cachePut, Type returnType, int retryCount) {
		super(joinPoint);
		this.lockKey = lockKey;
		this.cacheQuery = cacheQuery;
		this.cachePut = cachePut;
		this.returnType = returnType;
		this.retryCount = retryCount;
	}

	public Supplier<String> cacheQuery() {
		return this.cacheQuery;
	}

	public Consumer<Object> cachePut() {
		return this.cachePut;
	}

	public Type returnType() {
		return this.returnType;
	}

	public String lockKey() {
		return this.lockKey;
	}

	public int retryCount() {
		return this.retryCount;
	}

}
