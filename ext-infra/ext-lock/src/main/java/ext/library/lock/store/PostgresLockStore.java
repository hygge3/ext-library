package ext.library.lock.store;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 PostgreSQL 咨询锁的锁存储
 * <p>
 * 用 {@code pg_try_advisory_lock(hashtextextended(key, 0))} 抢会话级咨询锁、{@code pg_advisory_unlock} 释放。
 * 咨询锁跟着「会话」走，所以持锁期间本实现会一直占用一条连接，解锁（或连接断掉、进程崩溃）时锁自动消失——
 * 这就是它不需要 TTL 的原因，也因此 {@code timeout} 参数被忽略。
 * <p>
 * 代价要说清楚：持锁期间这条连接不能还给连接池，所以并发抢锁的数量受连接池大小限制，
 * 不要拿它做「上百个并发锁 + 小连接池」的场景，那种场景该用 Redis 后端。
 * <p>
 * 可重入按线程记账：同一线程对同一 key 再次加锁只计数 +1，最后一次解锁才真正释放。
 *
 * @since 4.0.0
 */
public class PostgresLockStore implements LockStore {

    /**
     * 抢锁 SQL：{@code hashtextextended} 把任意字符串稳定地映射成 bigint 锁 id
     */
    private static final String TRY_LOCK_SQL = "SELECT pg_try_advisory_lock(hashtextextended(?, 0))";

    /**
     * 释放 SQL
     */
    private static final String UNLOCK_SQL = "SELECT pg_advisory_unlock(hashtextextended(?, 0))";

    private final DataSource dataSource;

    /**
     * 当前线程持有的锁：key → 持有记录
     */
    private final ThreadLocal<Map<String, Holding>> holds = ThreadLocal.withInitial(HashMap::new);

    public PostgresLockStore(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean tryLock(String lockKey, Duration timeout, Duration waitTime, Duration loopInterval) {
        Map<String, Holding> held = holds.get();
        Holding holding = held.get(lockKey);
        if (holding != null) {
            // 同线程重入，直接计数 +1
            holding.count++;
            return true;
        }

        boolean wait = waitTime != null && !waitTime.isZero() && !waitTime.isNegative();
        long deadline = wait ? System.nanoTime() + waitTime.toNanos() : 0;
        Duration interval = loopInterval == null || loopInterval.isZero() || loopInterval.isNegative()
                ? Duration.ofMillis(10)
                : loopInterval;

        while (true) {
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                if (!tryAdvisoryLock(connection, lockKey)) {
                    closeQuietly(connection);
                    connection = null;
                    if (!wait || System.nanoTime() >= deadline) {
                        return false;
                    }
                    Thread.sleep(interval.toMillis());
                    continue;
                }
                // 抢到了：这条连接必须一直握着，锁才在
                held.put(lockKey, new Holding(connection));
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                closeQuietly(connection);
                Logs.warn(EmojiSymbol.LOCK, "等待分布式锁时被中断，lockKey: {}", lockKey);
                return false;
            } catch (SQLException e) {
                closeQuietly(connection);
                throw new ExtException(EmojiSymbol.LOCK, e, "获取 PostgreSQL 咨询锁异常，lockKey: {}", lockKey);
            }
        }
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
            advisoryUnlock(holding.connection, lockKey);
        } finally {
            // 连接关掉后会话结束，咨询锁也会随之释放，这里只是及时归还
            closeQuietly(holding.connection);
            held.remove(lockKey);
            if (held.isEmpty()) {
                holds.remove();
            }
        }
    }

    /**
     * 尝试在指定会话上抢咨询锁
     */
    private boolean tryAdvisoryLock(Connection connection, String lockKey) throws SQLException {
        // 咨询锁是会话级的，必须在自动提交模式下才能「跨语句」保持，不能挂在别人的事务里
        if (!connection.getAutoCommit()) {
            connection.setAutoCommit(true);
        }
        try (PreparedStatement ps = connection.prepareStatement(TRY_LOCK_SQL)) {
            ps.setString(1, lockKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    /**
     * 释放咨询锁
     */
    private void advisoryUnlock(Connection connection, String lockKey) {
        try (PreparedStatement ps = connection.prepareStatement(UNLOCK_SQL)) {
            ps.setString(1, lockKey);
            ps.executeQuery().close();
        } catch (SQLException e) {
            Logs.warn(EmojiSymbol.LOCK, "释放 PostgreSQL 咨询锁失败（连接关闭时会自动释放），lockKey: {}, 原因: {}", lockKey, e.getMessage());
        }
    }

    /**
     * 静默关闭连接
     */
    private void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException e) {
            Logs.warn(EmojiSymbol.LOCK, "关闭锁连接失败: {}", e.getMessage());
        }
    }

    /**
     * 某个 key 的持有记录（连同会话连接）
     */
    private static final class Holding {

        private final Connection connection;
        private int count = 1;

        private Holding(Connection connection) {
            this.connection = connection;
        }

    }

}
