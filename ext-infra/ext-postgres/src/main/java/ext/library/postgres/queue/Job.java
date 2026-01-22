package ext.library.postgres.queue;

import ext.library.json.util.JsonUtil;

import java.time.Instant;

/**
 * 任务实体
 *
 * @param id           任务 ID
 * @param queue        队列名称
 * @param payload      任务负载（JSON 字符串）
 * @param attempts     已尝试次数
 * @param maxAttempts  最大尝试次数
 * @param status       任务状态
 * @param error        错误信息
 * @param scheduledAt  计划执行时间
 * @param startedAt    开始执行时间
 * @param completedAt  完成时间
 * @param createdAt    创建时间
 * @since 4.0.0
 */
public record Job(
        long id,
        String queue,
        String payload,
        int attempts,
        int maxAttempts,
        JobStatus status,
        String error,
        Instant scheduledAt,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {

    /**
     * 解析 payload 为指定类型
     *
     * @param clazz 目标类型
     * @return 解析后的对象
     */
    public <T> T getPayload(Class<T> clazz) {
        return JsonUtil.readObj(payload, clazz);
    }

    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return attempts < maxAttempts;
    }

    /**
     * 是否是最后一次尝试
     */
    public boolean isLastAttempt() {
        return attempts >= maxAttempts;
    }
}
