package ext.library.tool.core;

import jakarta.validation.constraints.Positive;

import ext.library.tool.$;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
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

    /**
     * InheritableThreadLocal 能让子线程继承父线程中已经设置的 ThreadLocal 值
     */
    static final InheritableThreadLocal<Map<String, Serializable>> THREAD_LOCAL = new InheritableThreadLocal<>();

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

    /**
     * 放入数据
     *
     * @param key   键
     * @param value 值
     */
    public void put(String key, Serializable value) {
        Map<String, Serializable> map = get();
        map.put(key, value);
    }

    /**
     * 获取已有的值
     *
     * @return {@code Map<String, Serializable> }
     */
    private Map<String, Serializable> get() {
        Map<String, Serializable> map = THREAD_LOCAL.get();
        if ($.isEmpty(map)) {
            map = new HashMap<>(0);
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 获得字符串
     *
     * @param key 键
     * @return {@link Serializable}
     */
    public Serializable get(String key) {
        return THREAD_LOCAL.get().get(key);
    }


    /**
     * 从当前线程的本地存储中移除指定键的值。
     *
     * @param key 需要移除的键
     * @return 被移除的值，若键不存在则返回 null
     */
    public Serializable remove(String key) {
        // 通过线程本地存储获取当前线程的上下文并执行移除操作
        return THREAD_LOCAL.get().remove(key);
    }


    /**
     * 清除当前线程的线程局部变量（ThreadLocal）中的值。
     *
     * <p>此方法调用 {@link ThreadLocal#remove()} 方法，移除当前线程中与此 ThreadLocal 关联的值。
     * 调用此方法后，当前线程将不再持有该 ThreadLocal 的值，避免潜在的内存泄漏问题。</p>
     *
     * @see ThreadLocal#remove()
     */
    public void clear() {
        // 移除当前线程中与 THREAD_LOCAL 关联的值
        THREAD_LOCAL.remove();
    }

}
