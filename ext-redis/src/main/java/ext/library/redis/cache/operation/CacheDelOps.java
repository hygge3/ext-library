package ext.library.redis.cache.operation;

import ext.library.redis.cache.operation.function.VoidMethod;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存删除操作
 */
public class CacheDelOps extends AbstractCacheOps {

	/**
	 * 删除缓存数据
	 */
	private final VoidMethod cacheDel;

	public CacheDelOps(ProceedingJoinPoint joinPoint, VoidMethod cacheDel) {
		super(joinPoint);
		this.cacheDel = cacheDel;
	}

	public VoidMethod cacheDel() {
		return this.cacheDel;
	}

}
