package ext.library.job;

import ext.library.postgres.queue.Job;
import ext.library.postgres.queue.PostgresQueue;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.util.List;
import java.util.Map;

/**
 * 队列任务执行器
 * <p>
 * 轮询各已注册队列并顺序消费，单个任务失败不影响同批次其余任务。
 *
 * @since 4.1.0
 */
public class JobExecutor {

    private final PostgresQueue queue;

    /**
     * 队列名 → 处理器
     */
    private final Map<String, JobHandler<?>> handlers;

    /**
     * 每轮每队列最大出队条数
     */
    private final int batchSize;

    public JobExecutor(PostgresQueue queue, Map<String, JobHandler<?>> handlers, int batchSize) {
        this.queue = queue;
        this.handlers = handlers;
        this.batchSize = batchSize;
    }

    /**
     * 执行一轮
     *
     * @return 本轮处理的任务数
     */
    public int runOnce() {
        int processed = 0;
        for (Map.Entry<String, JobHandler<?>> entry : handlers.entrySet()) {
            processed += runOnce(entry.getKey(), entry.getValue());
        }
        return processed;
    }

    /**
     * 执行单个队列的一轮
     *
     * @param queueName 队列名
     * @param handler   处理器
     *
     * @return 处理的任务数
     */
    private int runOnce(String queueName, JobHandler<?> handler) {
        List<Job> jobs;
        try {
            jobs = queue.dequeue(queueName, batchSize);
        } catch (RuntimeException e) {
            Logs.warn(EmojiSymbol.JOB, e, "任务出队失败：queue={}", queueName);
            return 0;
        }
        for (Job job : jobs) {
            execute(job, handler);
        }
        return jobs.size();
    }

    /**
     * 执行单个任务并回写结果
     * <p>
     * 处理器抛出的异常交由队列记录，按剩余尝试次数决定重排或置为终态。
     */
    private void execute(Job job, JobHandler<?> handler) {
        try {
            invoke(handler, job.getPayload(handler.payloadType()));
            queue.complete(job.id());
        } catch (Exception e) {
            queue.fail(job.id(), e.getMessage());
            Logs.error(EmojiSymbol.JOB, e, "任务执行失败：queue={}, id={}, attempts={}/{}", job.queue(), job.id(),
                    job.attempts(), job.maxAttempts());
        }
    }

    /**
     * 通过类型擦除调用处理器
     */
    @SuppressWarnings("unchecked")
    private static void invoke(JobHandler<?> handler, Object payload) {
        ((JobHandler<Object>) handler).handle(payload);
    }

}
