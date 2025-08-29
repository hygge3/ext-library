package ext.library.ratelimiter.aspect;

import ext.library.ratelimiter.annotation.RateLimit;
import ext.library.ratelimiter.handler.IRateLimitHandler;
import ext.library.tool.core.Exceptions;
import ext.library.tool.util.ObjectUtil;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import jakarta.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 速率限制拦截切面处理类
 */
@Aspect
@AllArgsConstructor
public class RateLimiterAspect {

    /**
     * 缓存方法上的源注解信息。减少反射的开销
     */
    private static final Map<String, RateLimit> RATE_LIMIT_MAP = new ConcurrentHashMap<>();

    private final IRateLimitHandler rateLimitHandler;

    /**
     * 限流注解切面
     *
     * @param pjp {@link ProceedingJoinPoint}
     *
     * @return {@link Object}
     *
     * @throws Throwable 限流异常
     */
    @Around("@annotation(ext.library.ratelimiter.annotation.RateLimit)")
    public Object interceptor(@Nonnull ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = getRateLimit(signature.getMethod(), method.getName());
        if (rateLimitHandler.proceed(rateLimit, pjp)) {
            return pjp.proceed();
        } else {
            throw Exceptions.throwOut(ObjectUtil.isEmpty(rateLimit.msg()) ? "触发限流" : rateLimit.msg());
        }
    }

    /**
     * 获取执行速率限制注解，缓存反射信息
     *
     * @param method          执行方法
     * @param classMethodName 执行类方法名
     *
     * @return 方法对应的注解源信息，如果有，直接返回，如果无，获取放入缓存。
     */
    public RateLimit getRateLimit(Method method, String classMethodName) {
        return RATE_LIMIT_MAP.computeIfAbsent(classMethodName, k -> method.getAnnotation(RateLimit.class));
    }

}