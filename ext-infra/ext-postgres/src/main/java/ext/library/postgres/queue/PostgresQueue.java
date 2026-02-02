package ext.library.postgres.queue;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
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

    private final JdbcClient jdbcClient;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresQueue(JdbcClient jdbcClient, PostgresProperties properties) {
        this.jdbcClient = jdbcClient;
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
        Long id = jdbcClient.sql("""
                INSERT INTO %s (queue, payload, scheduled_at, max_attempts)
                VALUES (?, ?::jsonb, ?, ?)
                RETURNING id
                """.formatted(tableName))
                .param(queue)
                .param(JsonUtil.toJson(payload))
                .param(Timestamp.from(scheduledAt))
                .param(maxAttempts)
                .query(Long.class)
                .optional()
                .orElse(-1L);
        Logs.debug(EmojiSymbol.POSTGRES, "任务入队: queue={}, id={}", queue, id);
        return id;
    }

    /**
     * 出队 - 获取一个待处理任务（使用 SKIP LOCKED）
     *
     * @param queue 队列名称
     * @return 任务，如果没有待处理任务则返回空
     */
    public Optional<Job> dequeue(String queue) {
        return jdbcClient.sql("""
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
                """.formatted(tableName, tableName, tableName, tableName))
                .param(queue)
                .query((rs, _) -> new Job(
                        rs.getLong("id"),
                        rs.getString("queue"),
                        rs.getString("payload"),
                        rs.getInt("attempts"),
                        rs.getInt("max_attempts"),
                        JobStatus.valueOf(rs.getString("status")),
                        rs.getString("error"),
                        getInstant(rs.getTimestamp("scheduled_at")),
                        getInstant(rs.getTimestamp("started_at")),
                        getInstant(rs.getTimestamp("completed_at")),
                        getInstant(rs.getTimestamp("created_at"))
                ))
                .optional()
                .map(job -> {
                    Logs.debug(EmojiSymbol.POSTGRES, "任务出队: queue={}, id={}, attempts={}", queue, job.id(), job.attempts());
                    return job;
                });
    }

    /**
     * 批量出队 - 获取多个待处理任务
     *
     * @param queue 队列名称
     * @param limit 最大获取数量
     * @return 任务列表
     */
    public List<Job> dequeue(String queue, int limit) {
        return jdbcClient.sql("""
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
                """.formatted(tableName, tableName, tableName, tableName))
                .param(queue)
                .param(limit)
                .query((rs, _) -> new Job(
                        rs.getLong("id"),
                        rs.getString("queue"),
                        rs.getString("payload"),
                        rs.getInt("attempts"),
                        rs.getInt("max_attempts"),
                        JobStatus.valueOf(rs.getString("status")),
                        rs.getString("error"),
                        getInstant(rs.getTimestamp("scheduled_at")),
                        getInstant(rs.getTimestamp("started_at")),
                        getInstant(rs.getTimestamp("completed_at")),
                        getInstant(rs.getTimestamp("created_at"))
                ))
                .list();
    }

    /**
     * 完成任务
     *
     * @param jobId 任务 ID
     */
    public void complete(long jobId) {
        jdbcClient.sql("UPDATE " + tableName + " SET status = 'COMPLETED', completed_at = NOW() WHERE id = ?")
                .param(jobId)
                .update();
        Logs.debug(EmojiSymbol.POSTGRES, "任务完成: id={}", jobId);
    }

    /**
     * 任务失败
     *
     * @param jobId 任务 ID
     * @param error 错误信息
     */
    public void fail(long jobId, String error) {
        jdbcClient.sql("""
                UPDATE %s
                SET status = CASE WHEN attempts >= max_attempts THEN 'FAILED' ELSE 'PENDING' END,
                    error = ?,
                    started_at = NULL
                WHERE id = ?
                """.formatted(tableName))
                .param(error)
                .param(jobId)
                .update();
        Logs.debug(EmojiSymbol.POSTGRES, "任务失败: id={}, error={}", jobId, error);
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
        int rows = jdbcClient.sql("DELETE FROM " + tableName + " WHERE id = ?")
                .param(jobId)
                .update();
        return rows > 0;
    }

    /**
     * 获取任务详情
     *
     * @param jobId 任务 ID
     * @return 任务详情
     */
    public Optional<Job> getJob(long jobId) {
        return jdbcClient.sql("SELECT * FROM " + tableName + " WHERE id = ?")
                .param(jobId)
                .query((rs, _) -> new Job(
                        rs.getLong("id"),
                        rs.getString("queue"),
                        rs.getString("payload"),
                        rs.getInt("attempts"),
                        rs.getInt("max_attempts"),
                        JobStatus.valueOf(rs.getString("status")),
                        rs.getString("error"),
                        getInstant(rs.getTimestamp("scheduled_at")),
                        getInstant(rs.getTimestamp("started_at")),
                        getInstant(rs.getTimestamp("completed_at")),
                        getInstant(rs.getTimestamp("created_at"))
                ))
                .optional();
    }

    /**
     * 获取队列中待处理任务数量
     *
     * @param queue 队列名称
     * @return 待处理任务数量
     */
    public long countPending(String queue) {
        return jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE queue = ? AND status = 'PENDING'")
                .param(queue)
                .query(Long.class)
                .single();
    }

    /**
     * 获取队列统计信息
     *
     * @param queue 队列名称
     * @return 各状态任务数量 [pending, processing, completed, failed]
     */
    public long[] getQueueStats(String queue) {
        return jdbcClient.sql("""
                SELECT
                    COUNT(*) FILTER (WHERE status = 'PENDING') AS pending,
                    COUNT(*) FILTER (WHERE status = 'PROCESSING') AS processing,
                    COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed,
                    COUNT(*) FILTER (WHERE status = 'FAILED') AS failed
                FROM %s
                WHERE queue = ?
                """.formatted(tableName))
                .param(queue)
                .query((rs, _) -> new long[]{
                        rs.getLong("pending"),
                        rs.getLong("processing"),
                        rs.getLong("completed"),
                        rs.getLong("failed")
                })
                .optional()
                .orElse(new long[]{0, 0, 0, 0});
    }

    /**
     * 清理已完成的任务
     *
     * @param queue     队列名称
     * @param olderThan 清理多久之前完成的任务
     * @return 清理的任务数量
     */
    public int cleanupCompleted(String queue, Duration olderThan) {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE queue = ? AND status = 'COMPLETED' AND completed_at < NOW() - ?::interval")
                .param(queue)
                .param(olderThan.toSeconds() + " seconds")
                .update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理已完成任务: queue={}, count={}", queue, deleted);
        }
        return deleted;
    }

    /**
     * 重置超时的处理中任务
     *
     * @param queue   队列名称
     * @param timeout 超时时间
     * @return 重置的任务数量
     */
    public int resetStuckJobs(String queue, Duration timeout) {
        int reset = jdbcClient.sql("""
                UPDATE %s
                SET status = 'PENDING', started_at = NULL
                WHERE queue = ?
                  AND status = 'PROCESSING'
                  AND started_at < NOW() - ?::interval
                """.formatted(tableName))
                .param(queue)
                .param(timeout.toSeconds() + " seconds")
                .update();
        if (reset > 0) {
            Logs.info(EmojiSymbol.POSTGRES, "重置超时任务: queue={}, count={}", queue, reset);
        }
        return reset;
    }

    /**
     * 从时间戳获取 Instant，处理空值
     */
    private Instant getInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }
}
