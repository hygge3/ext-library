package ext.library.idempotent;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.core.metrics.MetricsRecorder;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * 幂等性控制切面
 * <p>
 * 通过 AOP 拦截标注了 {@link Idempotent} 或 {@link RepeatSubmit} 注解的方法。两者只差在键怎么来：
 * {@link Idempotent} 由业务表达式推导（同一笔业务只生效一次），{@link RepeatSubmit} 由「用户 + 接口 + 参数指纹」推导（连点只放行一次），
 * 写入缓存后的保证是同一套。
 * <p>
 * 幂等键交给 {@link CacheStrategy#putIfAbsent} 写入——这是后端的原子操作（Caffeine
 * {@code asMap().putIfAbsent}、Redis {@code SET NX PX}、PostgreSQL {@code INSERT … ON CONFLICT DO NOTHING}），
 * 并发重复请求只有一个能写入成功。不用 {@code get} + {@code put} 自己拼，那样并发下会双双放行。
 *
 * @param cacheStrategy 缓存策略（幂等键的存储后端）
 * @param keyGenerator  幂等键生成器
 * @param metrics       指标记录；为 null 时不统计
 */
@Aspect
public record IdempotentAspect(CacheStrategy cacheStrategy, KeyGenerator keyGenerator, MetricsRecorder metrics) {

    /**
     * 幂等键写入的缓存值（只用于占位，值本身没有含义）
     */
    private static final String PRESENT = "1";

    private static final Logger LOG = LoggerFactory.getLogger(IdempotentAspect.class);

    private static final RepeatSubmitKeyGenerator REPEAT_SUBMIT_KEY_GENERATOR = new RepeatSubmitKeyGenerator();

    /**
     * 环绕通知：幂等校验
     *
     * @param joinPoint 连接点
     * @param idempotent 幂等注解
     *
     * @return 方法执行结果
     *
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        return guard(
                joinPoint,
                idempotent.cacheName(),
                keyGenerator.generate(joinPoint, idempotent),
                DateUtil.convert(idempotent.duration(), idempotent.timeUnit()),
                idempotent.message(),
                idempotent.removeKeyWhenFinished()
        );
    }

    /**
     * 环绕通知：防重复提交
     *
     * @param joinPoint    连接点
     * @param repeatSubmit 防重提交注解
     *
     * @return 方法执行结果
     *
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(repeatSubmit)")
    public Object aroundRepeatSubmit(ProceedingJoinPoint joinPoint, RepeatSubmit repeatSubmit) throws Throwable {
        return guard(
                joinPoint,
                repeatSubmit.cacheName(),
                REPEAT_SUBMIT_KEY_GENERATOR.generate(joinPoint, repeatSubmit),
                DateUtil.convert(repeatSubmit.duration(), repeatSubmit.timeUnit()),
                repeatSubmit.message(),
                false
        );
    }

    /**
     * 占键 → 执行业务 → 按结果释放
     *
     * @param joinPoint          连接点
     * @param cacheName          缓存名
     * @param key                键
     * @param ttl                键存活时长
     * @param message            重复时的提示
     * @param removeOnFinished   业务成功后是否释放键
     *
     * @return 方法执行结果
     *
     * @throws Throwable 方法执行异常
     */
    private Object guard(ProceedingJoinPoint joinPoint,
                         String cacheName,
                         String key,
                         Duration ttl,
                         String message,
                         boolean removeOnFinished) throws Throwable {
        // 原子写入：写不进去说明时间窗内已经有同键请求放行过
        boolean saved = cacheStrategy.putIfAbsent(cacheName, key, PRESENT, ttl);

        if (!saved) {
            if (metrics != null) {
                metrics.deduplicated(joinPoint.getSignature().toShortString());
            }
            throw new ExtException(EmojiSymbol.IDEMPOTENT, message);
        }

        try {
            Object result = joinPoint.proceed();

            // 业务完成后，根据配置决定是否删除键
            if (removeOnFinished) {
                evictQuietly(cacheName, key);
            }

            return result;
        } catch (Exception e) {
            // 业务失败必须释放 key：参数校验、下游超时这类可恢复错误，用户重试是正当行为，
            // 不释放会让他在整个时间窗内都被判为重复提交。防暴力重试用 @RateLimit，不是靠幂等键锁人。
            evictQuietly(cacheName, key);
            throw e;
        }
    }

    /**
     * 删除幂等键，失败只记日志
     * <p>
     * 直接调 {@code evict} 的话，缓存抖动会用缓存异常盖掉真正的业务异常。
     *
     * @param cacheName 缓存名
     * @param key       幂等键
     */
    private void evictQuietly(String cacheName, String key) {
        try {
            cacheStrategy.evict(cacheName, key);
        } catch (Exception e) {
            LOG.warn("幂等键释放失败：cacheName={}, key={}", cacheName, key, e);
        }
    }

}
