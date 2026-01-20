package ext.library.tool.core;

import ext.library.tool.constant.EmojiSymbol;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 线程相关工具类
 */
public final class Threads {
    /** 原始堆栈索引位置，用于获取调用方法的堆栈信息 */
    private static final int ORIGIN_STACK_INDEX = 2;

    private Threads() {
    }

    /**
     * sleep 等待，单位为毫秒
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * sleep 等待
     *
     * @param duration 等待时长
     */
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 停止线程池
     * <p>
     * 先使用 shutdown 停止接收新任务并尝试完成所有已存在任务。
     * 如果超时，则调用 shutdownNow 取消在 workQueue 中 Pending 的任务，并中断所有阻塞函数。
     * 如果仍然超时，则强制退出。另对在 shutdown 时线程本身被调用中断做了处理。
     *
     * @param pool 线程池
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        if (!pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        Logs.warn(EmojiSymbol.TOOL, "线程池未停止");
                    }
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 打印线程异常信息
     */
    public static void printException(@Nullable Runnable r, @Nullable Throwable t) {
        if (t == null && r instanceof Future<?> future) {
            try {
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            Logs.error(EmojiSymbol.TOOL, t, t.getMessage());
        }
    }

    /**
     * 获取调用者所在的文件名
     *
     * @return 文件名
     */
    public static @Nullable String getFileName() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getFileName();
    }

    /**
     * 获取调用者所在的类名称
     *
     * @return 类名称
     */
    public static String getClassName() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getClassName();
    }

    /**
     * 获取调用者所在的方法名称
     *
     * @return 方法名称
     */
    public static String getMethodName() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getMethodName();
    }

    /**
     * 获取调用者所在的行号
     *
     * @return 行号
     */
    public static int getLineNumber() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getLineNumber();
    }
}
