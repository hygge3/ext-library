package ext.library.tool.util;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ToolException;
import ext.library.tool.runtime.Logs;
import ext.library.tool.runtime.Threads;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <h2>重试工具类</h2>
 *
 * @author GFire
 * @since 2025/4/22 17:58
 */
public abstract class RetryUtils {
    /**
     * 失败重试
     *
     * @param task            执行的任务，无返回值
     * @param acceptException 可接受的异常类型，执行的任务抛出此异常（及其子类）则失败重试。null 表示不接受任何异常
     * @param maxRetryCount   最大重试次数
     * @param waitTime        重试间隔等待时间，单位毫秒，<=0 则不等待
     *
     */
    public static void doWithRetry(Runnable task, Class<? extends Throwable> acceptException, int maxRetryCount, int waitTime) {
        doWithRetry(() -> {
            task.run();
            return 1;
        }, i -> i == 1, acceptException, maxRetryCount, waitTime);
    }

    /**
     * 失败重试
     *
     * @param task            执行的任务，有返回值
     * @param isValid         判断任务返回值是否合法，不合法则失败重试
     * @param acceptException 可接受的异常类型，执行的任务抛出此异常（及其子类）则失败重试。null 表示不接受任何异常
     * @param maxRetryCount   最大重试次数
     * @param waitTime        重试间隔等待时间，单位毫秒，<=0 则不等待
     *
     * @return supplier 执行结果
     *
     */
    public static <T> T doWithRetry(Supplier<T> task, Predicate<T> isValid, Class<? extends Throwable> acceptException, int maxRetryCount, int waitTime) {
        if (maxRetryCount <= 0) {
            throw new IllegalArgumentException("maxRetryCount must be > 0");
        }

        T result = null;
        for (int tryCount = 1; tryCount <= maxRetryCount; tryCount++) {
            try {
                result = task.get();
                if (isValid.test(result)) {
                    return result;
                } else {
                    Logs.error(EmojiSymbol.TOOL, "结果无效，重试 {} 次，结果： {}", tryCount, result);
                }
            } catch (Throwable e) {
                handleException(e, acceptException, maxRetryCount, tryCount);
            }

            if (waitTime > 0 && tryCount < maxRetryCount) {
                Threads.sleep(waitTime); // 等待一段时间后重试
            }
        }
        throw new ToolException(EmojiSymbol.TOOL, "结果： {}", result);
    }

    private static void handleException(Throwable e, Class<? extends Throwable> acceptException, int maxRetryCount, int tryCount) {
        Logs.error(EmojiSymbol.TOOL, e, "重试 {} 次", tryCount);
        if (acceptException.isInstance(e)) {
            if (tryCount == maxRetryCount) { // 最后一次重试仍失败，则抛出
                throw new ToolException(EmojiSymbol.TOOL, e, "已达最大尝试次数:{}", maxRetryCount);
            }
        } else { // 不可接受的异常，直接抛出
            throw new ToolException(EmojiSymbol.TOOL, e, "不可接受的异常，停止重试");
        }
    }

}
