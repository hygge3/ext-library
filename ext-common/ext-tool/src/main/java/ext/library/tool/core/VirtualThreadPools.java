package ext.library.tool.core;

import ext.library.tool.constant.EmojiSymbol;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * 虚拟线程池
 *
 * @since 2025.08.25
 */
public final class VirtualThreadPools {
    /** 默认任务名称，用于标识虚拟线程任务的默认名称 */
    private static final String DEFAULT_NAME = "虚拟线程任务";

    /** 单例模式的线程池实例，每个任务分配一个虚拟线程执行 */
    private static final ExecutorService INSTANCE = Executors.newVirtualThreadPerTaskExecutor();

    private VirtualThreadPools() {
    }

    /**
     * 线程池是否运行中
     *
     * @return 如果线程池正在运行返回 true，否则返回 false
     */
    public static boolean isRunning() {
        return !INSTANCE.isShutdown() && !INSTANCE.isTerminated();
    }

    /**
     * 执行给定的 Runnable 任务
     * <p>
     * 调用内部实现的执行方法，使用默认名称执行任务
     *
     * @param runnable 要执行的任务
     */
    public static void execute(Runnable runnable) {
        execute(DEFAULT_NAME, runnable);
    }

    /**
     * 执行给定的 Runnable 任务
     *
     * @param name     任务名称
     * @param runnable 要执行的任务
     */
    public static void execute(String name, Runnable runnable) {
        INSTANCE.execute(wrapRunnable(name, runnable));
    }

    /**
     * 异步执行给定的 Supplier 任务，并返回一个 CompletableFuture
     * <p>
     * 该方法用于启动一个异步任务，任务由传入的 Supplier 实现，使用默认的线程池执行。
     *
     * @param supplier 用于生成结果的 Supplier 实现
     * @param <T>      返回值类型
     * @return 返回一个 CompletableFuture 对象，用于处理异步任务的结果
     */
    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return async(DEFAULT_NAME, supplier);
    }

    /**
     * 异步执行给定的 Supplier 任务，并返回一个 CompletableFuture
     *
     * @param name     任务名称
     * @param supplier 用于生成结果的 Supplier 实现
     * @param <T>      返回值类型
     * @return 返回一个 CompletableFuture 对象，用于处理异步任务的结果
     */
    public static <T> CompletableFuture<T> async(String name, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(wrapSupplier(name, supplier), INSTANCE);
    }

    /**
     * 提交一个 Callable 任务并返回其 Future 对象
     * <p>
     * 该方法用于将 Callable 类型的任务提交到线程池中执行，并返回一个 Future 对象，
     * 用于获取任务执行结果或监控任务状态。
     *
     * @param callable 要提交的 Callable 任务
     * @param <T>      返回值类型
     * @return 与提交任务关联的 Future 对象
     */
    public static <T> Future<T> submit(Callable<T> callable) {
        return submit(DEFAULT_NAME, callable);
    }

    /**
     * 提交一个带有指定线程名称的 Callable 任务到线程池中执行
     * <p>
     * 该方法用于将 Callable 任务提交到线程池中执行，并在执行过程中设置指定的线程名称，
     * 同时保留并恢复 MDC 上下文信息。
     *
     * @param name     线程执行时的名称
     * @param callable 要执行的 Callable 任务
     * @param <T>      返回值类型
     * @return 一个 Future 对象，用于获取任务执行结果
     */
    public static <T> Future<T> submit(String name, Callable<T> callable) {
        return INSTANCE.submit(wrapCallable(name, callable));
    }

    /**
     * 关闭线程池并等待所有任务完成
     * <p>
     * 调用此方法将关闭指定的线程池，并确保所有已提交的任务执行完毕。
     */
    public static void shutdown() {
        Threads.shutdownAndAwaitTermination(INSTANCE);
    }

    // region 包装方法

    /**
     * 包装 Runnable，添加 MDC 传递、线程名称设置和异常处理
     */
    private static Runnable wrapRunnable(String name, Runnable runnable) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return () -> {
            Thread thread = Thread.currentThread();
            String oldName = thread.getName();
            thread.setName(name);
            if (!CollectionUtils.isEmpty(mdcContext)) {
                MDC.setContextMap(mdcContext);
            }
            try {
                runnable.run();
            } catch (Throwable t) {
                Logs.error(EmojiSymbol.TOOL, t, "线程池内线程异常");
            } finally {
                thread.setName(oldName);
                MDC.clear();
            }
        };
    }

    /**
     * 包装 Supplier，添加 MDC 传递、线程名称设置和异常处理
     */
    private static <T> Supplier<T> wrapSupplier(String name, Supplier<T> supplier) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return () -> {
            Thread thread = Thread.currentThread();
            String oldName = thread.getName();
            thread.setName(name);
            if (!CollectionUtils.isEmpty(mdcContext)) {
                MDC.setContextMap(mdcContext);
            }
            try {
                return supplier.get();
            } catch (Throwable t) {
                Logs.error(EmojiSymbol.TOOL, t, "线程池内线程异常");
                throw t;
            } finally {
                thread.setName(oldName);
                MDC.clear();
            }
        };
    }

    /**
     * 包装 Callable，添加 MDC 传递、线程名称设置和异常处理
     */
    private static <T> Callable<T> wrapCallable(String name, Callable<T> callable) {
        Map<String, String> mdcContext = MDC.getCopyOfContextMap();
        return () -> {
            Thread thread = Thread.currentThread();
            String oldName = thread.getName();
            thread.setName(name);
            if (!CollectionUtils.isEmpty(mdcContext)) {
                MDC.setContextMap(mdcContext);
            }
            try {
                return callable.call();
            } catch (Exception e) {
                Logs.error(EmojiSymbol.TOOL, e, "线程池内线程异常");
                throw e;
            } finally {
                thread.setName(oldName);
                MDC.clear();
            }
        };
    }

    // endregion
}
