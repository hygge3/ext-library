package ext.library.idempotent;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.DateUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 幂等性控制切面
 * <p>
 * 通过 AOP 拦截标注了 {@link Idempotent} 注解的方法，实现幂等性控制。
 * <p>
 * 幂等键交给 {@link CacheStrategy#putIfAbsent} 写入——这是后端的原子操作（Caffeine
 * {@code asMap().putIfAbsent}、Redis {@code SET NX PX}、PostgreSQL {@code INSERT … ON CONFLICT DO NOTHING}），
 * 并发重复请求只有一个能写入成功。不用 {@code get} + {@code put} 自己拼，那样并发下会双双放行。
 *
 * @param cacheStrategy 缓存策略（幂等键的存储后端）
 * @param keyGenerator  幂等键生成器
 */
@Aspect
public record IdempotentAspect(CacheStrategy cacheStrategy, KeyGenerator keyGenerator) {

    /**
     * 幂等键写入的缓存值（只用于占位，值本身没有含义）
     */
    private static final String PRESENT = "1";

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
        // 生成幂等 key
        String idempotentKey = keyGenerator.generate(joinPoint, idempotent);
        String cacheName = idempotent.cacheName();

        // 原子写入：写不进去说明时间窗内已经有同键请求放行过
        boolean saved = cacheStrategy.putIfAbsent(
                cacheName,
                idempotentKey,
                PRESENT,
                DateUtil.convert(idempotent.duration(), idempotent.timeUnit())
        );

        if (!saved) {
            throw new ExtException(EmojiSymbol.IDEMPOTENT, idempotent.message());
        }

        try {
            Object result = joinPoint.proceed();

            // 业务完成后，根据配置决定是否删除幂等 key
            if (idempotent.removeKeyWhenFinished()) {
                cacheStrategy.evict(cacheName, idempotentKey);
            }

            return result;
        } catch (Throwable e) {
            // 异常时，根据配置决定是否删除幂等 key
            if (idempotent.removeKeyWhenError()) {
                cacheStrategy.evict(cacheName, idempotentKey);
            }
            throw e;
        }
    }

}
