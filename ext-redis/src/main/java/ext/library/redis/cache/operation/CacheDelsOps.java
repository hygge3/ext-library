package ext.library.redis.cache.operation;

import ext.library.redis.cache.operation.function.VoidMethod;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存删除操作
 */
public class CacheDelsOps extends AbstractCacheOps {

	/**
	 * 删除缓存数据
	 */
	private final VoidMethod[] cacheDels;

	public CacheDelsOps(ProceedingJoinPoint joinPoint, VoidMethod[] cacheDels) {
		super(joinPoint);
		this.cacheDels = cacheDels;
	}

	public VoidMethod[] cacheDel() {
		return this.cacheDels;
	}

}
