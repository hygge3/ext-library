package ext.library.tool.core;


import jakarta.annotation.Nonnull;

import ext.library.tool.$;
import ext.library.tool.constant.Holder;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/**
 * 公共线程池
 */
@UtilityClass
@Slf4j
public class ThreadPools {

    static final Integer QUEUE_MAX = 10;

    /**
     * Singleton instance to use.
     */
    public static ThreadPoolExecutor INSTANCE = new ThreadPoolExecutor(
            // 核心线程数大小。不论是否空闲都存在的线程
            Holder.CPU_CORE_NUM,
            // 最大线程数 - 100 个
            100,
            // 存活时间。非核心线程数如果空闲指定时间。就回收
            // 存活时间不宜过长。避免任务量遇到尖峰情况时。大量空闲线程占用资源
            QUEUE_MAX,
            // 存活时间的单位
            TimeUnit.SECONDS,
            // 等待任务存放队列 - 队列最大值
            // 这样配置。当积压任务数量为 队列最大值 时。会创建新线程来执行任务。直到线程总数达到 最大线程数
            new LinkedBlockingQueue<>(QUEUE_MAX),
            // 新线程创建工厂 - LinkedBlockingQueue 不支持线程优先级。所以直接新增线程就可以了
            Thread.ofVirtual().factory(),
            // 拒绝策略 - 在主线程继续执行。
            new ThreadPoolExecutor.CallerRunsPolicy());


    public void update(@Nonnull ThreadPoolExecutor executor) {
        INSTANCE = executor;
    }

    /**
     * 线程池是否运行中
     */
    public boolean isRunning() {
        return !INSTANCE.isShutdown() && !INSTANCE.isTerminated() && !INSTANCE.isTerminating();
    }

    /**
     * 核心线程数
     */
    public long getCorePoolSize() {
        return INSTANCE.getCorePoolSize();
    }

    /**
     * 活跃线程数
     */
    public long getActiveCount() {
        return INSTANCE.getActiveCount();
    }

    /**
     * 已执行任务总数
     */
    public long getTaskCount() {
        return INSTANCE.getTaskCount();
    }

    /**
     * 允许的最大线程数量
     */
    public long getMaximumPoolSize() {
        return INSTANCE.getMaximumPoolSize();
    }

    /**
     * 是否可能触发拒绝策略，仅为估算
     */
    public boolean isReject() {
        long activeCount = getActiveCount();
        long size = getMaximumPoolSize();

        // 活跃线程占比未达到 90% 不可能
        long per = activeCount / size;
        if (per <= 90) {
            return false;
        }

        // 占比达到 90% 的情况下，剩余可用线程数小于 10 则可能触发拒绝
        return size - activeCount < QUEUE_MAX;
    }

    public void execute(@Nonnull Runnable runnable) {
        execute(null, runnable);
    }

    /**
     * 执行
     *
     * @param name     任务名
     * @param runnable 任务
     */
    public void execute(String name, @Nonnull Runnable runnable) {
        // 获取当前线程的配置
        Map<String, String> map = MDC.getCopyOfContextMap();
        INSTANCE.execute(() -> {
            Thread thread = Thread.currentThread();
            String oldName = thread.getName();
            if ($.isNotBlank(name)) {
                thread.setName(name);
            }
            // 存在则填充
            if ($.isNotEmpty(map)) {
                MDC.setContextMap(map);
            }
            try {
                runnable.run();
            } catch (Throwable throwable) {
                log.error("[🛠️] The thread inside the thread pool is abnormal!", throwable);
            } finally {
                thread.setName(oldName);
                MDC.clear();
            }
        });
    }

    public <T> CompletableFuture<T> async(@Nonnull Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, INSTANCE);
    }

    public <T> Future<T> submit(@Nonnull Callable<T> callable) {
        return INSTANCE.submit(callable);
    }

}
