package ext.library.cache.core;

import ext.library.cache.annotion.Cache;
import ext.library.cache.enums.CacheType;
import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.util.spel.SpelUtil;
import ext.library.tool.util.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Aspect
public class CacheAspect {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CacheStrategy cacheStrategy;

    public CacheAspect(CacheStrategy cacheStrategy) {this.cacheStrategy = cacheStrategy;}

    @Pointcut("@annotation(ext.library.cache.annotion.Cache)")
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

        // 强制更新
        if (annotation.type() == CacheType.PUT) {
            Object object = point.proceed();
            cacheStrategy.put(cacheName, key, object);
            return object;
        }
        // 删除
        else if (annotation.type() == CacheType.DELETE) {
            cacheStrategy.evict(cacheName, key);
            return point.proceed();
        }
        Object cache = cacheStrategy.get(cacheName, key, signature.getReturnType());
        if (Objects.nonNull(cache)) {
            return cache;
        }

        log.debug("[💾] get data from database");
        Object object = point.proceed();
        if (Objects.nonNull(object)) {
            cacheStrategy.put(cacheName, key, object, DateUtil.convert(annotation.timeout(), TimeUnit.SECONDS));
        }
        return object;
    }
}