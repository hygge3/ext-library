package ext.library.lock;

import ext.library.lock.store.LockStore;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.time.Duration;

/**
 * 分布式锁切面
 * <p>
 * 通过 AOP 拦截标注了 {@link RedisLock} 注解的方法，在方法执行前加锁、执行后释放。
 * 加锁原语交给 {@link LockStore}，因此注解名虽然还叫 {@code @RedisLock}，内部并不绑定 Redis。
 *
 * @param lockStore    锁存储
 * @param keyGenerator 锁 key 生成器
 */
@Aspect
public record RedisLockAspect(LockStore lockStore, LockKeyGenerator keyGenerator) {

    /**
     * 环绕通知：加锁 → 执行 → 释放
     *
     * @param joinPoint 连接点
     * @param redisLock 锁注解
     *
     * @return 方法执行结果
     *
     * @throws Throwable 方法执行异常
     */
    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        // 生成锁 key
        String lockKey = keyGenerator.generate(joinPoint, redisLock);
        Duration timeout = Duration.of(redisLock.timeout(), redisLock.timeUnit().toChronoUnit());
        Duration waitTime = Duration.of(redisLock.waitTime(), redisLock.waitTimeUnit().toChronoUnit());
        Duration loopInterval = Duration.of(redisLock.loopInterval(), redisLock.loopIntervalUnit().toChronoUnit());

        boolean acquired = lockStore.tryLock(lockKey, timeout, waitTime, loopInterval);
        if (!acquired) {
            Logs.debug(EmojiSymbol.LOCK, "获取分布式锁失败，lockKey: {}", lockKey);
            throw new LockException(redisLock.message(), lockKey);
        }
        try {
            return joinPoint.proceed();
        } finally {
            // 正常返回与抛异常都走这里释放锁
            lockStore.unlock(lockKey);
        }
    }

}
