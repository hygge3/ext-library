package ext.library.lock.store;

import ext.library.redis.util.DistributedLock;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 Redis 的锁存储
 * <p>
 * 加锁/解锁原语来自 [ext-redis] 的 {@link DistributedLock}（{@code SET NX PX} 加锁、Lua 校验值后释放，
 * 锁自带 TTL，进程崩了也会自动过期）。可重入在本类里按线程记账：同一线程对同一 key 再次加锁只是计数 +1，
 * 不会再去抢一次，最后一次解锁才真正删 key。
 *
 * @since 4.0.0
 */
public class RedisLockStore implements LockStore {

    /**
     * 当前线程持有的锁：key → 持有记录
     */
    private final ThreadLocal<Map<String, Holding>> holds = ThreadLocal.withInitial(HashMap::new);

    @Override
    public boolean tryLock(String lockKey, Duration timeout, Duration waitTime, Duration loopInterval) {
        Map<String, Holding> held = holds.get();
        Holding holding = held.get(lockKey);
        if (holding != null) {
            // 同线程重入，直接计数 +1
            holding.count++;
            return true;
        }

        // DistributedLock 内部会自动拼接 lockPrefix，这里补一个分隔符
        DistributedLock lock = new DistributedLock(":" + lockKey, timeout, loopInterval);
        boolean acquired;
        try {
            // 不等待则只尝试一次，否则限时自旋重试
            acquired = waitTime == null || waitTime.isZero() || waitTime.isNegative()
                    ? lock.tryLock()
                    : lock.tryLock(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Logs.warn(EmojiSymbol.LOCK, "等待分布式锁时被中断，lockKey: {}", lockKey);
            return false;
        } catch (Exception e) {
            throw new ExtException(EmojiSymbol.LOCK, e, "获取分布式锁异常，lockKey: {}", lockKey);
        }

        if (acquired) {
            held.put(lockKey, new Holding(lock));
        }
        return acquired;
    }

    @Override
    public void unlock(String lockKey) {
        Map<String, Holding> held = holds.get();
        Holding holding = held.get(lockKey);
        if (holding == null) {
            Logs.warn(EmojiSymbol.LOCK, "释放了一个未持有的分布式锁，lockKey: {}", lockKey);
            return;
        }
        if (--holding.count > 0) {
            return;
        }
        try {
            holding.lock.unlock();
        } finally {
            held.remove(lockKey);
            if (held.isEmpty()) {
                holds.remove();
            }
        }
    }

    /**
     * 某个 key 的持有记录
     */
    private static final class Holding {

        private final DistributedLock lock;
        private int count = 1;

        private Holding(DistributedLock lock) {
            this.lock = lock;
        }

    }

}
