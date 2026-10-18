package ext.library.lock;

import ext.library.redis.util.DistributedLock;
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
 */
@Aspect
public record RedisLockAspect(LockKeyGenerator keyGenerator) {

    @Around("@annotation(redisLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedisLock redisLock) throws Throwable {
        // 生成锁 key
        String lockKey = keyGenerator.generate(joinPoint, redisLock);
        Duration timeout = Duration.of(redisLock.timeout(), redisLock.timeUnit().toChronoUnit());
        Duration loopInterval = Duration.of(redisLock.loopInterval(), redisLock.loopIntervalUnit().toChronoUnit());

        // DistributedLock 内部会自动拼接 lockPrefix 前缀，这里补一个分隔符
        try (DistributedLock lock = new DistributedLock(":" + lockKey, timeout, loopInterval)) {
            // 不等待则只尝试一次，否则限时自旋重试
            boolean acquired = redisLock.waitTime() <= 0
                    ? lock.tryLock()
                    : lock.tryLock(Duration.of(redisLock.waitTime(), redisLock.waitTimeUnit().toChronoUnit()));
            if (!acquired) {
                Logs.debug(EmojiSymbol.LOCK, "获取分布式锁失败，lockKey: {}", lockKey);
                throw new LockException(redisLock.message(), lockKey);
            }
            // try-with-resources 退出时调 close()，close() 内部即 unlock()
            return joinPoint.proceed();
        }
    }

}
