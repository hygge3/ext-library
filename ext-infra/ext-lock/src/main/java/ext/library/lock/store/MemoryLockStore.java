package ext.library.lock.store;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存锁存储
 * <p>
 * 只在单实例下成立：多实例部署时各进程各锁各的，等于没有锁。后端为 {@code MEMORY} 时才会用它。
 *
 * @since 4.2.0
 */
public class MemoryLockStore implements LockStore {

    private final Map<String, Holding> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String lockKey, Duration timeout, Duration waitTime, Duration loopInterval) {
        long deadline = System.nanoTime() + Math.max(0, waitTime.toNanos());
        while (true) {
            if (acquire(lockKey, timeout)) {
                return true;
            }
            if (System.nanoTime() >= deadline) {
                return false;
            }
            try {
                Thread.sleep(Math.max(1, loopInterval.toMillis()));
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    @Override
    public void unlock(String lockKey) {
        locks.computeIfPresent(lockKey,
                (k, holding) -> holding.owner() == Thread.currentThread() ? holding.release() : holding);
    }

    private boolean acquire(String lockKey, Duration timeout) {
        Thread self = Thread.currentThread();
        boolean[] acquired = {false};
        locks.compute(lockKey, (k, holding) -> {
            if (holding == null || holding.isStale()) {
                acquired[0] = true;
                return new Holding(self, 1, expireAt(timeout));
            }
            // 同一线程重复加锁只把计数加一，不续期，否则嵌套调用会让锁无限延长
            if (holding.owner() == self) {
                acquired[0] = true;
                return holding.reenter();
            }
            return holding;
        });
        return acquired[0];
    }

    private static long expireAt(Duration timeout) {
        return System.nanoTime() + timeout.toNanos();
    }

    /**
     * 锁的持有记录
     *
     * @param owner         持有线程
     * @param depth         重入次数
     * @param expireAtNanos 到期时刻（{@link System#nanoTime()}）
     */
    private record Holding(Thread owner, int depth, long expireAtNanos) {

        boolean isStale() {
            return expireAtNanos - System.nanoTime() <= 0;
        }

        Holding reenter() {
            return new Holding(owner, depth + 1, expireAtNanos);
        }

        Holding release() {
            return depth <= 1 ? null : new Holding(owner, depth - 1, expireAtNanos);
        }
    }

}
