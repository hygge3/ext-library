package ext.library.cache.core;

import ext.library.cache.annotation.Cache;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.util.spel.SpelUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 缓存切面
 * <p>
 * 拦截 {@link Cache} 注解标记的方法，根据缓存类型执行相应的缓存操作
 *
 * @since 2025.08.29
 */
@Aspect
public class CacheAspect {

    private final CacheStrategy cacheStrategy;

    public CacheAspect(CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    @Pointcut("@annotation(ext.library.cache.annotation.Cache)")
    public void cacheAspect() {
    }

    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Object[] args = point.getArgs();

        Cache annotation = method.getAnnotation(Cache.class);
        String cacheName = annotation.cacheName();
        String key = SpelUtil.parseValueToString(point.getThis(), method, args, annotation.key());

        return switch (annotation.type()) {
            case PUT -> {
                Object result = point.proceed();
                cacheStrategy.put(cacheName, key, result);
                yield result;
            }
            case DELETE -> {
                cacheStrategy.evict(cacheName, key);
                yield point.proceed();
            }
            case FULL -> getOrLoad(point, signature, cacheName, key, annotation);
        };
    }

    /**
     * 获取缓存或加载数据
     */
    private Object getOrLoad(ProceedingJoinPoint point, MethodSignature signature,
                             String cacheName, String key, Cache annotation) throws Throwable {
        Object cache = cacheStrategy.get(cacheName, key, signature.getReturnType());
        if (cache != null) {
            return cache;
        }

        Logs.debug(EmojiSymbol.CACHE, "从数据库获取数据");
        Object result = point.proceed();
        if (result != null) {
            cacheStrategy.put(cacheName, key, result, DateUtil.convert(annotation.timeout(), annotation.timeUnit()));
        }
        return result;
    }
}
