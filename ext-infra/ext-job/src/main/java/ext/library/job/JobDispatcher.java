package ext.library.job;

import ext.library.postgres.queue.PostgresQueue;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 队列任务投递器
 * <p>
 * 供业务代码投递任务，实际入队由 {@link PostgresQueue} 完成。
 *
 * @since 4.1.0
 */
public class JobDispatcher {

    /**
     * 默认队列名
     */
    public static final String DEFAULT_QUEUE = "default";

    private final PostgresQueue queue;

    /**
     * 队列名 → 处理器，投递前校验队列是否有消费者
     */
    private final Map<String, JobHandler<?>> handlers;

    public JobDispatcher(PostgresQueue queue, List<JobHandler<?>> handlers) {
        this.queue = queue;
        this.handlers = new HashMap<>(handlers.size());
        for (JobHandler<?> handler : handlers) {
            String name = handlerQueueName(handler);
            if (this.handlers.put(name, handler) != null) {
                Logs.warn(EmojiSymbol.JOB, "同一队列注册了多个处理器，后者生效：queue={}", name);
            }
        }
        Logs.info(EmojiSymbol.JOB, "载入任务处理器：count={}", this.handlers.size());
    }

    /**
     * 立即投递
     *
     * @param payload 负载
     * @param <T>     负载类型泛型
     *
     * @return 任务 ID
     */
    public <T> long dispatch(T payload) {
        return dispatch(DEFAULT_QUEUE, payload);
    }

    /**
     * 立即投递到指定队列
     *
     * @param queueName 队列名
     * @param payload   负载
     * @param <T>       负载类型泛型
     *
     * @return 任务 ID
     */
    public <T> long dispatch(String queueName, T payload) {
        requireHandler(queueName);
        return queue.enqueue(queueName, payload);
    }

    /**
     * 延迟投递
     *
     * @param queueName 队列名
     * @param payload   负载
     * @param delay     延迟时间
     * @param <T>       负载类型泛型
     *
     * @return 任务 ID
     */
    public <T> long dispatch(String queueName, T payload, Duration delay) {
        requireHandler(queueName);
        return queue.enqueue(queueName, payload, delay);
    }

    /**
     * 延迟投递到默认队列
     *
     * @param payload 负载
     * @param delay   延迟时间
     * @param <T>     负载类型泛型
     *
     * @return 任务 ID
     */
    public <T> long dispatch(T payload, Duration delay) {
        return dispatch(DEFAULT_QUEUE, payload, delay);
    }

    /**
     * 指定最大重试次数投递
     *
     * @param queueName   队列名
     * @param payload     负载
     * @param maxAttempts 最大尝试次数
     * @param <T>         负载类型泛型
     *
     * @return 任务 ID
     */
    public <T> long dispatch(String queueName, T payload, int maxAttempts) {
        requireHandler(queueName);
        return queue.enqueue(queueName, payload, Instant.now(), maxAttempts);
    }

    /**
     * 查询待处理任务数
     *
     * @param queueName 队列名
     *
     * @return 待处理任务数
     */
    public long countPending(String queueName) {
        return queue.countPending(queueName);
    }

    /**
     * 查询队列统计
     *
     * @param queueName 队列名
     *
     * @return 各状态任务数 [pending, processing, completed, failed]
     */
    public long[] stats(String queueName) {
        return queue.getQueueStats(queueName);
    }

    /**
     * 清理已完成任务
     *
     * @param queueName 队列名
     * @param olderThan 保留时长
     *
     * @return 清理条数
     */
    public int cleanup(String queueName, Duration olderThan) {
        return queue.cleanupCompleted(queueName, olderThan);
    }

    /**
     * 重置卡住的任务
     * <p>
     * 进程崩溃时任务会残留在 {@code PROCESSING} 状态，启动时调用本方法恢复。
     *
     * @param queueName 队列名
     * @param timeout   超时判定时长
     *
     * @return 重置条数
     */
    public int resetStuck(String queueName, Duration timeout) {
        return queue.resetStuckJobs(queueName, timeout);
    }

    /**
     * 已注册的队列名
     *
     * @return 队列名集合
     */
    public Set<String> queues() {
        return Collections.unmodifiableSet(handlers.keySet());
    }

    /**
     * 队列名 → 处理器的只读视图
     * <p>
     * 供 {@link JobExecutor} 轮询消费，避免执行器重复解析队列名与处理器的对应关系。
     *
     * @return 只读映射
     */
    public Map<String, JobHandler<?>> handlers() {
        return Collections.unmodifiableMap(handlers);
    }

    /**
     * 是否没有注册任何处理器
     *
     * @return 无处理器时为 true
     */
    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    /**
     * 校验队列是否已注册处理器
     * <p>
     * 未注册就投递的任务只会堆积在表中直到重试耗尽，因此提前失败。
     */
    private void requireHandler(String queueName) {
        if (!handlers.containsKey(queueName)) {
            throw new IllegalArgumentException("队列未注册任务处理器：" + queueName);
        }
    }

    /**
     * 取队列名
     */
    private static String handlerQueueName(JobHandler<?> handler) {
        String queueName = handler.queue();
        if (queueName == null || queueName.isBlank()) {
            throw new IllegalArgumentException(
                    "任务处理器队列名不能为空：" + handler.payloadType().getName());
        }
        return queueName;
    }

}
