package ext.library.postgres.queue;

/**
 * 任务状态枚举
 *
 * @since 4.0.0
 */
public enum JobStatus {

    /**
     * 待处理
     */
    PENDING,

    /**
     * 处理中
     */
    PROCESSING,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 已失败（达到最大重试次数）
     */
    FAILED
}
