package ext.library.tool.core;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Nonnull;
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
public class VirtualThreads {

    private static final ExecutorService INSTANCE = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 执行
     *
     * @param runnable 任务
     */
    public void execute(@Nonnull Runnable runnable) {
        INSTANCE.execute(runnable);
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