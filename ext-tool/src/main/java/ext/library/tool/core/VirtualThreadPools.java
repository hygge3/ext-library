package ext.library.tool.core;

import ext.library.tool.util.ObjectUtil;
import ext.library.tool.util.StringUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import jakarta.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * 虚拟线程
 *
 * @since 2025.08.25
 */
@Slf4j
@UtilityClass
public class VirtualThreadPools {

    private static final ExecutorService INSTANCE = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 线程池是否运行中
     */
    public boolean isRunning() {
        return !INSTANCE.isShutdown() && !INSTANCE.isTerminated();
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
            if (StringUtil.isNotBlank(name)) {
                thread.setName(name);
            }
            // 存在则填充
            if (ObjectUtil.isNotEmpty(map)) {
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

    public void shutdown() {
        Threads.shutdownAndAwaitTermination(INSTANCE);
    }
}