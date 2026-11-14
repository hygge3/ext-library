package ext.library.resilience.ratelimit;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.metrics.MetricsRecorder;
import ext.library.core.token.TokenBucketResult;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 令牌桶限流切面
 * <p>
 * 桶状态由 {@link CacheStrategy#tryAcquire} 在后端原子维护；L2 时只用分布式那一级，
 * 否则每个实例各限各的，多机部署下总速率会翻倍。
 */
@Aspect
public class RateLimitAspect {

    /**
     * 限流计数所在缓存区
     */
    public static final String CACHE_NAME = "ratelimit";

    private static final String DEFAULT_MESSAGE = "请求过于频繁，请稍后再试";

    private final CacheStrategy cacheStrategy;
    private final RateLimitKeyGenerator keyGenerator;
    private final int warnThreshold;
    private final MetricsRecorder metrics;

    /**
     * 每个 key 上次告警的时间（nanos），用来把「每秒上千条 WARN」压成每个窗口一条
     */
    private final Map<String, Long> warnAt = new ConcurrentHashMap<>();

    /**
     * @param cacheStrategy 缓存策略，令牌桶落在这里
     * @param keyGenerator  Key 生成器
     * @param warnThreshold 用量达到该百分比时告警，0 关闭
     * @param metrics       指标记录；为 null 时不统计
     */
    public RateLimitAspect(CacheStrategy cacheStrategy, RateLimitKeyGenerator keyGenerator, int warnThreshold,
                           MetricsRecorder metrics) {
        this.cacheStrategy = cacheStrategy;
        this.keyGenerator = keyGenerator;
        this.warnThreshold = warnThreshold;
        this.metrics = metrics;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Duration refillPeriod = Duration.of(rateLimit.window(), rateLimit.timeUnit().toChronoUnit());
        checkPermits(joinPoint, rateLimit);
        String key = keyGenerator.generate(joinPoint, rateLimit);
        TokenBucketResult result = cacheStrategy.distributed()
                .tryAcquire(CACHE_NAME, key, rateLimit.permits(), rateLimit.limit(), refillPeriod);
        if (!result.allowed()) {
            if (metrics != null) {
                metrics.rejected(rateLimit.prefix());
            }
            String message = rateLimit.message().isBlank() ? DEFAULT_MESSAGE : rateLimit.message();
            throw new RateLimitException(message, key, result.remaining(), result.retryAfterMillis());
        }

        warnIfNearLimit(joinPoint, rateLimit, key, result.remaining(), refillPeriod);
        return joinPoint.proceed();
    }

    /**
     * permits 大于 limit 是配置错误，每个方法只查一次
     */
    private void checkPermits(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        if (rateLimit.permits() <= rateLimit.limit()) {
            return;
        }
        throw new IllegalStateException("@RateLimit(permits) 不能大于 limit：" + joinPoint.getSignature().toShortString());
    }

    /**
     * 桶快空了先告警：等被拒才开始查就晚了。热路径上只在该 key 本窗口首次越线时打一条。
     */
    private void warnIfNearLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit, String key, long remaining,
                                 Duration refillPeriod) {
        if (warnThreshold <= 0) {
            return;
        }
        long used = rateLimit.limit() - remaining;
        if (used * 100 < rateLimit.limit() * warnThreshold) {
            return;
        }
        long now = System.nanoTime();
        Long last = warnAt.get(key);
        if (last != null && now - last < refillPeriod.toNanos()) {
            return;
        }
        warnAt.put(key, now);
        Logs.warn(EmojiSymbol.RATELIMIT, "令牌桶用量已达 {}%：{}（{}）剩余 {}/{}（阈值 {}%）",
                used * 100 / rateLimit.limit(), joinPoint.getSignature().toShortString(), key,
                remaining, rateLimit.limit(), warnThreshold);
    }

}
