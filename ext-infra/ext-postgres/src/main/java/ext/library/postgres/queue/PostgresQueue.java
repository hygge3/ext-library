package ext.library.postgres.queue;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PostgreSQL 队列实现
 * <p>
 * 使用 {@code FOR UPDATE SKIP LOCKED} 实现无锁任务队列
 *
 * @since 4.0.0
 */
public class PostgresQueue {

    private final DataSource dataSource;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresQueue(DataSource dataSource, PostgresProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.tableName = properties.getQueueTableName();
    }

    /**
     * 入队 - 立即执行
     *
     * @param queue   队列名称
     * @param payload 任务负载
     * @return 任务 ID
     */
    public long enqueue(String queue, Object payload) {
        return enqueue(queue, payload, Instant.now());
    }

    /**
     * 入队 - 延迟执行
     *
     * @param queue   队列名称
     * @param payload 任务负载
     * @param delay   延迟时间
     * @return 任务 ID
     */
    public long enqueue(String queue, Object payload, Duration delay) {
        return enqueue(queue, payload, Instant.now().plus(delay));
    }

    /**
     * 入队 - 指定时间执行
     *
     * @param queue       队列名称
     * @param payload     任务负载
     * @param scheduledAt 计划执行时间
     * @return 任务 ID
     */
    public long enqueue(String queue, Object payload, Instant scheduledAt) {
        return enqueue(queue, payload, scheduledAt, properties.getQueueMaxAttempts());
    }

    /**
     * 入队 - 完整参数
     *
     * @param queue       队列名称
     * @param payload     任务负载
     * @param scheduledAt 计划执行时间
     * @param maxAttempts 最大重试次数
     * @return 任务 ID
     */
    public long enqueue(String queue, Object payload, Instant scheduledAt, int maxAttempts) {
        String sql = """
                INSERT INTO %s (queue, payload, scheduled_at, max_attempts)
                VALUES (?, ?::jsonb, ?, ?)
                RETURNING id
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            ps.setString(2, JsonUtil.toJson(payload));
            ps.setTimestamp(3, Timestamp.from(scheduledAt));
            ps.setInt(4, maxAttempts);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    Logs.debug(EmojiSymbol.POSTGRES, "任务入队: queue={}, id={}", queue, id);
                    return id;
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "任务入队失败: queue={}", queue);
            throw new RuntimeException("Failed to enqueue job to: " + queue, e);
        }
        return -1;
    }

    /**
     * 出队 - 获取一个待处理任务（使用 SKIP LOCKED）
     *
     * @param queue 队列名称
     * @return 任务，如果没有待处理任务则返回空
     */
    public Optional<Job> dequeue(String queue) {
        String sql = """
                WITH next_job AS (
                    SELECT id FROM %s
                    WHERE queue = ?
                      AND status = 'PENDING'
                      AND attempts < max_attempts
                      AND scheduled_at <= NOW()
                    ORDER BY scheduled_at
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                )
                UPDATE %s
                SET attempts = attempts + 1,
                    status = 'PROCESSING',
                    started_at = NOW()
                FROM next_job
                WHERE %s.id = next_job.id
                RETURNING %s.*
                """.formatted(tableName, tableName, tableName, tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Job job = mapToJob(rs);
                    Logs.debug(EmojiSymbol.POSTGRES, "任务出队: queue={}, id={}, attempts={}", queue, job.id(), job.attempts());
                    return Optional.of(job);
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "任务出队失败: queue={}", queue);
            throw new RuntimeException("Failed to dequeue job from: " + queue, e);
        }
        return Optional.empty();
    }

    /**
     * 批量出队 - 获取多个待处理任务
     *
     * @param queue 队列名称
     * @param limit 最大获取数量
     * @return 任务列表
     */
    public List<Job> dequeue(String queue, int limit) {
        String sql = """
                WITH next_jobs AS (
                    SELECT id FROM %s
                    WHERE queue = ?
                      AND status = 'PENDING'
                      AND attempts < max_attempts
                      AND scheduled_at <= NOW()
                    ORDER BY scheduled_at
                    LIMIT ?
                    FOR UPDATE SKIP LOCKED
                )
                UPDATE %s
                SET attempts = attempts + 1,
                    status = 'PROCESSING',
                    started_at = NOW()
                FROM next_jobs
                WHERE %s.id = next_jobs.id
                RETURNING %s.*
                """.formatted(tableName, tableName, tableName, tableName);
        List<Job> jobs = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapToJob(rs));
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "批量任务出队失败: queue={}", queue);
            throw new RuntimeException("Failed to dequeue jobs from: " + queue, e);
        }
        return jobs;
    }

    /**
     * 完成任务
     *
     * @param jobId 任务 ID
     */
    public void complete(long jobId) {
        String sql = "UPDATE " + tableName + " SET status = 'COMPLETED', completed_at = NOW() WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            ps.executeUpdate();
            Logs.debug(EmojiSymbol.POSTGRES, "任务完成: id={}", jobId);
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "标记任务完成失败: id={}", jobId);
            throw new RuntimeException("Failed to complete job: " + jobId, e);
        }
    }

    /**
     * 任务失败
     *
     * @param jobId 任务 ID
     * @param error 错误信息
     */
    public void fail(long jobId, String error) {
        String sql = """
                UPDATE %s
                SET status = CASE WHEN attempts >= max_attempts THEN 'FAILED' ELSE 'PENDING' END,
                    error = ?,
                    started_at = NULL
                WHERE id = ?
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, error);
            ps.setLong(2, jobId);
            ps.executeUpdate();
            Logs.debug(EmojiSymbol.POSTGRES, "任务失败: id={}, error={}", jobId, error);
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "标记任务失败失败: id={}", jobId);
            throw new RuntimeException("Failed to mark job as failed: " + jobId, e);
        }
    }

    /**
     * 任务失败（使用异常）
     *
     * @param jobId 任务 ID
     * @param e     异常
     */
    public void fail(long jobId, Throwable e) {
        fail(jobId, e.getMessage());
    }

    /**
     * 删除任务
     *
     * @param jobId 任务 ID
     * @return 是否删除成功
     */
    public boolean delete(long jobId) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "删除任务失败: id={}", jobId);
            throw new RuntimeException("Failed to delete job: " + jobId, e);
        }
    }

    /**
     * 获取任务详情
     *
     * @param jobId 任务 ID
     * @return 任务详情
     */
    public Optional<Job> getJob(long jobId) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jobId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapToJob(rs));
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取任务失败: id={}", jobId);
            throw new RuntimeException("Failed to get job: " + jobId, e);
        }
        return Optional.empty();
    }

    /**
     * 获取队列中待处理任务数量
     *
     * @param queue 队列名称
     * @return 待处理任务数量
     */
    public long countPending(String queue) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE queue = ? AND status = 'PENDING'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取待处理任务数量失败: queue={}", queue);
            throw new RuntimeException("Failed to count pending jobs in: " + queue, e);
        }
        return 0;
    }

    /**
     * 获取队列统计信息
     *
     * @param queue 队列名称
     * @return 各状态任务数量 [pending, processing, completed, failed]
     */
    public long[] getQueueStats(String queue) {
        String sql = """
                SELECT
                    COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
                    COUNT(*) FILTER (WHERE status = 'PROCESSING') AS processing,
                    COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed,
                    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed
                FROM %s
                WHERE queue = ?
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new long[]{
                            rs.getLong("pending"),
                            rs.getLong("processing"),
                            rs.getLong("completed"),
                            rs.getLong("failed")
                    };
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取队列统计失败: queue={}", queue);
            throw new RuntimeException("Failed to get queue stats: " + queue, e);
        }
        return new long[]{0, 0, 0, 0};
    }

    /**
     * 清理已完成的任务
     *
     * @param queue     队列名称
     * @param olderThan 清理多久之前完成的任务
     * @return 清理的任务数量
     */
    public int cleanupCompleted(String queue, Duration olderThan) {
        String sql = "DELETE FROM " + tableName + " WHERE queue = ? AND status = 'COMPLETED' AND completed_at < NOW() - ?::interval";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            ps.setString(2, olderThan.toSeconds() + " seconds");
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "清理已完成任务: queue={}, count={}", queue, deleted);
            }
            return deleted;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "清理已完成任务失败: queue={}", queue);
            throw new RuntimeException("Failed to cleanup completed jobs in: " + queue, e);
        }
    }

    /**
     * 重置超时的处理中任务
     *
     * @param queue   队列名称
     * @param timeout 超时时间
     * @return 重置的任务数量
     */
    public int resetStuckJobs(String queue, Duration timeout) {
        String sql = """
                UPDATE %s
                SET status = 'PENDING', started_at = NULL
                WHERE queue = ?
                  AND status = 'PROCESSING'
                  AND started_at < NOW() - ?::interval
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, queue);
            ps.setString(2, timeout.toSeconds() + " seconds");
            int reset = ps.executeUpdate();
            if (reset > 0) {
                Logs.info(EmojiSymbol.POSTGRES, "重置超时任务: queue={}, count={}", queue, reset);
            }
            return reset;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "重置超时任务失败: queue={}", queue);
            throw new RuntimeException("Failed to reset stuck jobs in: " + queue, e);
        }
    }

    /**
     * 将查询结果映射为任务对象
     */
    private Job mapToJob(ResultSet rs) throws SQLException {
        return new Job(
                rs.getLong("id"),
                rs.getString("queue"),
                rs.getString("payload"),
                rs.getInt("attempts"),
                rs.getInt("max_attempts"),
                JobStatus.valueOf(rs.getString("status")),
                rs.getString("error"),
                getInstant(rs, "scheduled_at"),
                getInstant(rs, "started_at"),
                getInstant(rs, "completed_at"),
                getInstant(rs, "created_at")
        );
    }

    /**
     * 从查询结果获取时间戳，处理空值
     */
    private Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}
