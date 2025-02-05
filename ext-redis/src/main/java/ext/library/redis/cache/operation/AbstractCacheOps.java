package ext.library.redis.cache.operation;

import ext.library.redis.config.properties.RedisPropertiesHolder;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 抽象缓存操作
 */
public abstract class AbstractCacheOps {

    protected AbstractCacheOps(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    private final ProceedingJoinPoint joinPoint;

    /**
     * 织入方法
     *
     * @return ProceedingJoinPoint
     */
    public ProceedingJoinPoint joinPoint() {
        return this.joinPoint;
    }

    /**
     * 检查缓存数据是否是空值
     *
     * @param cacheData 缓存数据
     * @return true: 是空值
     */
    public boolean nullValue(Object cacheData) {
        return RedisPropertiesHolder.nullValue().equals(cacheData);
    }

}
