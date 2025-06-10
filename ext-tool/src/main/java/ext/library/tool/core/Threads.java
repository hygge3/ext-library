package ext.library.tool.core;

import jakarta.validation.constraints.Positive;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 线程相关工具类。
 */
@Slf4j
@UtilityClass
public class Threads {

    /**
     * sleep 等待，单位为毫秒
     */
    public void sleep(@Positive long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 停止线程池 先使用 shutdown, 停止接收新任务并尝试完成所有已存在任务。如果超时，则调用 shutdownNow, 取消在 workQueue 中
     * Pending 的任务，并中断所有阻塞函数。如果仍然超時，則強制退出。另对在 shutdown 时线程本身被调用中断做了处理。
     */
    public void shutdownAndAwaitTermination(ExecutorService pool) {
        if (pool != null && !pool.isShutdown()) {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        log.info("[🛠️] Pool did not terminate");
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
    public void printException(Runnable r, Throwable t) {
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
            log.error(t.getMessage(), t);
        }
    }

    static final int ORIGIN_STACK_INDEX = 2;

    public String getFileName() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getFileName();
    }

    /**
     * 得到当前线程所在的类名称
     *
     * @return 类名称
     */
    public String getClassName() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getClassName();
    }

    /**
     * 得到当前线程所在的方法名称
     *
     * @return 方法名称
     */
    public String getMethodName() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getMethodName();
    }

    /**
     * 得到当前线程在第几行
     *
     * @return 第几行
     */
    public int getLineNumber() {
        return Thread.currentThread().getStackTrace()[ORIGIN_STACK_INDEX].getLineNumber();
    }

}
