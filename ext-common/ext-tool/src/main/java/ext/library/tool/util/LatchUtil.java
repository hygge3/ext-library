package ext.library.tool.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 并发任务同步工具类
 * <p>
 * 简化 {@link CountDownLatch} 的使用，提供任务提交和等待完成的便捷方式。
 * <p>
 * 使用流程：
 * <ol>
 *   <li>创建 LatchUtil 实例</li>
 *   <li>调用 {@link #submitTask(Executor, Runnable)} 提交任务</li>
 *   <li>调用 {@link #waitFor(long, TimeUnit)} 等待所有任务完成</li>
 * </ol>
 * <p>
 * 示例：
 * <pre>{@code
 * LatchUtil latch = new LatchUtil();
 * latch.submitTask(executor, () -> doTask1());
 * latch.submitTask(executor, () -> doTask2());
 * boolean completed = latch.waitFor(10, TimeUnit.SECONDS);
 * }</pre>
 * <p>
 * 注意：每个 LatchUtil 实例使用 ThreadLocal 存储任务，同一线程内的任务互不干扰。
 *
 * @since 2025.01.01
 */
public final class LatchUtil {

    /**
     * 使用 ThreadLocal 存储当前线程的任务列表
     */
    private final ThreadLocal<List<TaskInfo>> taskHolder = ThreadLocal.withInitial(ArrayList::new);

    /**
     * 提交任务
     *
     * @param executor 执行器
     * @param runnable 任务
     */
    public void submitTask(Executor executor, Runnable runnable) {
        taskHolder.get().add(new TaskInfo(executor, runnable));
    }

    /**
     * 等待所有已提交的任务完成（无超时限制）
     * <p>
     * 注意：如果任务未完成，此方法将一直阻塞
     *
     * @return 如果所有任务完成返回 true，如果被中断返回 false
     */
    public boolean waitFor() {
        return waitFor(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    /**
     * 等待所有已提交的任务完成，或直到超时
     *
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @return 如果所有任务在超时前完成返回 true，否则返回 false
     */
    public boolean waitFor(long timeout, TimeUnit timeUnit) {
        List<TaskInfo> tasks = popTasks();
        if (tasks.isEmpty()) {
            return true;
        }

        CountDownLatch latch = new CountDownLatch(tasks.size());
        for (TaskInfo task : tasks) {
            task.executor.execute(() -> {
                try {
                    task.runnable.run();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 获取并清除当前线程的任务列表
     */
    private List<TaskInfo> popTasks() {
        List<TaskInfo> tasks = taskHolder.get();
        taskHolder.remove();
        return tasks;
    }

    /**
     * 任务信息
     */
    private record TaskInfo(Executor executor, Runnable runnable) {
    }

}
