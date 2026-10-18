package ext.library.ratelimit;

import ext.library.ratelimit.store.RateLimitStore;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.Duration;

/**
 * 接口限流切面
 * <p>
 * 通过 AOP 拦截标注了 {@link RateLimit} 注解的方法，在方法执行前完成一次限流判定。
 */
@Aspect
public record RateLimitAspect(RateLimitStore rateLimitStore, RateLimitKeyGenerator keyGenerator) {

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // 生成限流 key
        String rateLimitKey = keyGenerator.generate(joinPoint, rateLimit);

        // 尝试获取许可，超阈值直接拒绝
        RateLimitResult result = rateLimitStore.increment(
                rateLimitKey,
                rateLimit.limit(),
                Duration.of(rateLimit.window(), rateLimit.timeUnit().toChronoUnit())
        );
        if (result.isDenied()) {
            Logs.debug(EmojiSymbol.RATELIMIT, "触发限流：{}，{}，剩余重试 {} 秒",
                    rateLimitKey, result, result.getRetryAfterSeconds());
            throw new RateLimitException(rateLimit.message(), rateLimitKey, result);
        }

        return joinPoint.proceed();
    }

}
