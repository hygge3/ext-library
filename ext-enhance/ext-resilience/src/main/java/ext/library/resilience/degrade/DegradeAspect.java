package ext.library.resilience.degrade;

import ext.library.core.context.ContextTaskDecorator;
import ext.library.core.util.SpringUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.task.TaskDecorator;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 熔断降级切面
 * <p>
 * 只在熔断打开时调用降级方法；原方法自己抛的异常照原样抛给调用方——
 * 把业务异常吞进降级会掩盖真实故障，降级只对「明确判定服务不可用」的熔断态生效。
 * <p>
 * 配了 {@code executor} 时原方法在独立线程池里执行，超时即取消并计为失败：
 * 一次慢调用也是「服务异常」的证据，该计入失败率。
 */
@Aspect
public class DegradeAspect {

    private final DegradeRegistry registry;
    private final ConcurrentHashMap<String, Method> fallbacks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Executor> executors = new ConcurrentHashMap<>();
    private final TaskDecorator taskDecorator = new ContextTaskDecorator();

    public DegradeAspect(DegradeRegistry registry) {
        this.registry = registry;
    }

    @Around("@annotation(degrade)")
    public Object around(ProceedingJoinPoint joinPoint, Degrade degrade) throws Throwable {
        validate(joinPoint, degrade);
        CircuitBreaker breaker = registry.get(breakerKey(joinPoint, degrade), degrade);
        if (!breaker.allowRequest()) {
            return invokeFallback(joinPoint, degrade);
        }
        try {
            Object result = call(joinPoint, degrade);
            breaker.onSuccess();
            return result;
        } catch (Throwable e) {
            breaker.onFailure();
            throw e;
        }
    }

    /**
     * 配置错了就在首次调用时炸掉，别等到熔断时才发现核心接口配了降级
     */
    private void validate(ProceedingJoinPoint joinPoint, Degrade degrade) {
        String target = joinPoint.getSignature().toShortString();
        if (degrade.critical() && !degrade.fallback().isBlank()) {
            throw new IllegalStateException("@Degrade 核心接口不允许配降级方法：%s（fallback=%s）"
                    .formatted(target, degrade.fallback()));
        }
        if (degrade.timeoutMillis() > 0 && degrade.executor().isBlank()) {
            throw new IllegalStateException("@Degrade(timeoutMillis) 需要同时指定 executor：%s".formatted(target));
        }
    }

    /**
     * 同步调用只能在别的线程上被中断，所以过池时提交到线程池再等结果
     */
    private Object call(ProceedingJoinPoint joinPoint, Degrade degrade) throws Throwable {
        if (degrade.executor().isBlank()) {
            return joinPoint.proceed();
        }
        CompletableFuture<Object> future = new CompletableFuture<>();
        executor(degrade.executor()).execute(taskDecorator.decorate(() -> {
            try {
                future.complete(joinPoint.proceed());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        }));
        try {
            return degrade.timeoutMillis() > 0
                    ? future.get(degrade.timeoutMillis(), TimeUnit.MILLISECONDS)
                    : future.get();
        } catch (TimeoutException _) {
            future.cancel(true);
            throw new IsolationTimeoutException(joinPoint.getSignature().toShortString(), degrade.timeoutMillis());
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待隔离线程池结果时被中断", e);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    private Executor executor(String name) {
        return executors.computeIfAbsent(name, n -> {
            Object bean;
            try {
                bean = SpringUtil.getBean(n);
            } catch (RuntimeException e) {
                throw new IllegalStateException("@Degrade(executor) 未找到线程池 bean：" + n, e);
            }
            if (!(bean instanceof Executor executor)) {
                throw new IllegalStateException("@Degrade(executor) 指向的 bean 不是 Executor：" + n);
            }
            return executor;
        });
    }

    /**
     * 熔断器名称：显式配置优先，否则按「类名#方法名」
     */
    private String breakerKey(ProceedingJoinPoint joinPoint, Degrade degrade) {
        if (!degrade.name().isBlank()) {
            return degrade.name();
        }
        return AopUtils.getTargetClass(joinPoint.getTarget()).getName() + "#" + joinPoint.getSignature().getName();
    }

    private Object invokeFallback(ProceedingJoinPoint joinPoint, Degrade degrade) throws Throwable {
        Method fallback = resolveFallback(joinPoint, degrade);
        try {
            return fallback.invoke(joinPoint.getTarget(), joinPoint.getArgs());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("降级方法不可访问: " + fallback, e);
        }
    }

    private Method resolveFallback(ProceedingJoinPoint joinPoint, Degrade degrade) {
        if (degrade.fallback().isBlank()) {
            throw new CircuitOpenException("服务熔断中且未配置降级方法: " + joinPoint.getSignature().toShortString());
        }
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        String cacheKey = targetClass.getName() + "#" + degrade.fallback();
        return fallbacks.computeIfAbsent(cacheKey, k -> locate(targetClass, joinPoint, degrade));
    }

    /**
     * 先按原方法参数表找，找不到再找空参版本；返回类型必须能赋给原方法返回类型
     */
    private Method locate(Class<?> targetClass, ProceedingJoinPoint joinPoint, Degrade degrade) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method fallback = find(targetClass, degrade.fallback(), signature.getParameterTypes());
        if (fallback == null) {
            fallback = find(targetClass, degrade.fallback());
        }
        if (fallback == null) {
            throw new IllegalStateException("未找到降级方法 %s#%s（参数表需同原方法或为空参）"
                    .formatted(targetClass.getName(), degrade.fallback()));
        }
        Class<?> returnType = signature.getReturnType();
        if (returnType != void.class && !returnType.isAssignableFrom(fallback.getReturnType())) {
            throw new IllegalStateException("降级方法 %s#%s 返回类型 %s 与原方法 %s 不兼容"
                    .formatted(targetClass.getName(), degrade.fallback(), fallback.getReturnType(), returnType));
        }
        ReflectionUtils.makeAccessible(fallback);
        return fallback;
    }

    /**
     * 沿父类链查找：多个 Controller 共用一个写在抽象基类里的降级方法很常见
     */
    private Method find(Class<?> targetClass, String name, Class<?>... parameterTypes) {
        for (Class<?> current = targetClass; current != null && current != Object.class; current = current.getSuperclass()) {
            try {
                return current.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException _) {
                // 继续往父类找
            }
        }
        return null;
    }

}
