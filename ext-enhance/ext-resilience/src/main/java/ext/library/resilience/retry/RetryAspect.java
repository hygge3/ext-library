package ext.library.resilience.retry;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 重试切面
 * <p>
 * 退避是「指数 × 抖动」：只做指数退避的话，同一时刻失败的一批调用会在同一时刻一起重试，
 * 退避等于把尖峰整体挪后；抖动把重试时刻散开，下游才有机会缓过来。
 */
@Aspect
public class RetryAspect {

    /**
     * 幂等注解的全限定名
     * <p>
     * 按名字判断而不是直接引用：为了一个校验把 ext-idempotent 拉成强制依赖不划算，
     * 没引幂等模块时这里自然全部不命中。
     */
    private static final Set<String> IDEMPOTENT_ANNOTATIONS = Set.of(
            "ext.library.idempotent.Idempotent",
            "ext.library.idempotent.RepeatSubmit");

    @Around("@annotation(retry)")
    public Object around(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        ensureIdempotent(joinPoint, retry);
        long delay = retry.initialDelayMillis();
        for (int attempt = 1; ; attempt++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (attempt >= retry.maxAttempts() || !retryable(retry, e)) {
                    throw e;
                }
                long wait = jittered(delay, retry.jitter());
                Logs.warn(EmojiSymbol.RETRY, "第 {}/{} 次调用失败，{}ms 后重试：{} - {}",
                        attempt, retry.maxAttempts(), wait, joinPoint.getSignature().toShortString(), e.getMessage());
                Thread.sleep(wait);
                delay = (long) (delay * retry.multiplier());
            }
        }
    }

    private static boolean retryable(Retry retry, Throwable e) {
        for (Class<? extends Throwable> type : retry.retryOn()) {
            if (type.isInstance(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 抖动的实际间隔：{@code jitter = 0} 固定为 delay，{@code = 1} 落在 [0, delay]
     */
    private static long jittered(long delay, double jitter) {
        if (jitter <= 0 || delay <= 0) {
            return delay;
        }
        double low = delay * (1 - jitter);
        return (long) (low + Math.random() * delay * jitter);
    }

    private void ensureIdempotent(ProceedingJoinPoint joinPoint, Retry retry) {
        if (retry.assumeIdempotent()) {
            return;
        }
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        if (hasIdempotentAnnotation(method.getAnnotations()) || hasIdempotentAnnotation(targetClass.getAnnotations())) {
            return;
        }
        throw new IllegalStateException(
                "@Retry 只能用在幂等接口上：%s#%s 缺少 @Idempotent / @RepeatSubmit（确认幂等可设 assumeIdempotent = true）"
                        .formatted(targetClass.getName(), method.getName()));
    }

    private static boolean hasIdempotentAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (IDEMPOTENT_ANNOTATIONS.contains(annotation.annotationType().getName())) {
                return true;
            }
        }
        return false;
    }

}
