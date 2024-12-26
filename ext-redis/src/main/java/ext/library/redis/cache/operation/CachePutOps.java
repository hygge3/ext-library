package ext.library.redis.cache.operation;

import java.util.function.Consumer;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存放置操作
 */
public class CachePutOps extends AbstractCacheOps {

	/**
	 * 向缓存写入数据
	 */
	private final Consumer<Object> cachePut;

	public CachePutOps(ProceedingJoinPoint joinPoint, Consumer<Object> cachePut) {
		super(joinPoint);
		this.cachePut = cachePut;
	}

	public Consumer<Object> cachePut() {
		return this.cachePut;
	}

}
