package ext.library.cache.core;

import ext.library.cache.annotion.L2Cache;
import ext.library.cache.util.CacheUtil;
import ext.library.core.util.spel.SpelUtil;
import ext.library.tool.constant.Symbol;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class CacheAspect {

    @Pointcut("@annotation(ext.library.cache.annotion.L2Cache)")
    public void cacheAspect() {
    }

    @Around("cacheAspect()")
    public Object doAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        Object[] args = point.getArgs();

        L2Cache annotation = method.getAnnotation(L2Cache.class);
        String elResult = SpelUtil.parseValueToString(point.getThis(), method, args, annotation.key());
        String realKey = annotation.cacheName() + Symbol.COLON + elResult;

        // 强制更新
        if (annotation.type() == CacheType.PUT) {
            Object object = point.proceed();
            CacheUtil.put(realKey, object, annotation.timeOut(), TimeUnit.SECONDS);
            return object;
        }
        // 删除
        else if (annotation.type() == CacheType.DELETE) {
            CacheUtil.evict(realKey);
            return point.proceed();
        }
        Object cache = CacheUtil.get(realKey, signature.getReturnType());
        if (Objects.nonNull(cache)) {
            return cache;
        }

        log.debug("[💾] get data from database");
        Object object = point.proceed();
        if (Objects.nonNull(object)) {
            CacheUtil.put(realKey, object, annotation.timeOut(), TimeUnit.SECONDS);
        }
        return object;
    }
}