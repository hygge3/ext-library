package ext.library.lock.store;

import java.time.Duration;

/**
 * 锁存储 SPI
 * <p>
 * 只描述「尝试加锁 / 解锁」这两个原语，具体协议由各后端实现：
 * <ul>
 *   <li>{@link RedisLockStore} —— Redis {@code SET NX PX} + Lua 校验后释放，锁自带 TTL</li>
 *   <li>{@link PostgresLockStore} —— {@code pg_try_advisory_lock} 会话级咨询锁，连接断开即释放</li>
 * </ul>
 * <p>
 * 实现必须支持「同一线程对同一个 key 重复加锁」（可重入）：注解切面在嵌套调用时不会重复去抢锁，
 * 靠的就是这一层。
 *
 * @since 4.0.0
 */
public interface LockStore {

    /**
     * 尝试获取锁
     *
     * @param lockKey      锁 key
     * @param timeout      锁的持有时长（仅对自带 TTL 的后端有意义，会话级锁会忽略）
     * @param waitTime     最长等待时长，{@code <= 0} 表示只尝试一次
     * @param loopInterval 等待期间的重试间隔
     *
     * @return {@code true} 获取成功
     */
    boolean tryLock(String lockKey, Duration timeout, Duration waitTime, Duration loopInterval);

    /**
     * 释放锁（只释放当前线程持有的那一层）
     *
     * @param lockKey 锁 key
     */
    void unlock(String lockKey);

}
