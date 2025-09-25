package ext.library.tool.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * LatchUtils 类用于简化线程同步操作，特别是与 CountDownLatch 相关的操作
 * 它提供了一种简便的方式来提交任务并等待它们完成，而不需要显式地创建和管理 CountDownLatch 实例
 * <p/>
 * 使用说明
 * <p/>
 * 1.提交任务：在主流程代码，先调用 LatchUtils.submitTask() 提交 Runnable 任务和其对应的 Executor（用来执行这个 Runnable）。
 * <p/>
 * 2.执行并等待：当并行任务都提交完毕后，只需调用一次 LatchUtils.waitFor()。该方法会立即触发所有已注册任务执行，并阻塞等待所有任务执行完成或超时。
 */
public class LatchUtil {
    // 使用 ThreadLocal 存储每个线程的任务信息列表，避免线程间的数据共享问题
    private static final ThreadLocal<List<TaskInfo>> THREADLOCAL = ThreadLocal.withInitial(LinkedList::new);

    /**
     * 提交一个任务到线程池执行
     *
     * @param executor 用于执行任务的执行器
     * @param runnable 要执行的任务
     */
    public static void submitTask(Executor executor, Runnable runnable) {
        THREADLOCAL.get().add(new TaskInfo(executor, runnable));
    }

    /**
     * 从当前线程的 ThreadLocal 中获取并移除任务信息列表
     *
     * @return 当前线程的任务信息列表
     */
    private static List<TaskInfo> popTask() {
        List<TaskInfo> taskInfos = THREADLOCAL.get();
        THREADLOCAL.remove();
        return taskInfos;
    }

    /**
     * 等待所有已提交的任务完成，或直到指定的超时时间结束
     *
     * @param timeout  等待的最长时间
     * @param timeUnit 时间单位
     *
     * @return 如果所有任务都完成则返回 true，否则返回 false
     */
    public static boolean waitFor(long timeout, TimeUnit timeUnit) {
        List<TaskInfo> taskInfos = popTask();
        if (taskInfos.isEmpty()) {
            return true;
        }
        CountDownLatch latch = new CountDownLatch(taskInfos.size());
        for (TaskInfo taskInfo : taskInfos) {
            Executor executor = taskInfo.executor;
            Runnable runnable = taskInfo.runnable;
            executor.execute(() -> {
                try {
                    runnable.run();
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean await = false;
        try {
            await = latch.await(timeout, timeUnit);
        } catch (Exception ignored) {
        }
        return await;
    }

    /**
     * TaskInfo 类封装了执行器和可运行的任务信息
     */
    private record TaskInfo(Executor executor, Runnable runnable) {
    }
}