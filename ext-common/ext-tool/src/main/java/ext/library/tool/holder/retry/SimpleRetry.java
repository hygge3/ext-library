package ext.library.tool.holder.retry;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.core.Logs;
import ext.library.tool.core.Threads;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.StringUtil;

import java.io.IOException;

/**
 * 简单重试实现
 * <p>
 * 提供固定次数、固定间隔的重试策略。
 *
 * @param maxAttempts 最大尝试次数
 * @param sleepMillis 重试间隔（毫秒）
 * @since 2025.01.01
 */
public record SimpleRetry(int maxAttempts, long sleepMillis) implements Retry {

    /** 默认最大尝试次数 */
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    /** 默认重试间隔（毫秒） */
    private static final long DEFAULT_SLEEP_MILLIS = 1L;

    /**
     * 使用默认配置构造（3 次尝试，1ms 间隔）
     */
    public SimpleRetry() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_SLEEP_MILLIS);
    }

    /**
     * 指定最大尝试次数，使用默认间隔
     *
     * @param maxAttempts 最大尝试次数
     */
    public SimpleRetry(int maxAttempts) {
        this(maxAttempts, DEFAULT_SLEEP_MILLIS);
    }

    /**
     * 规范构造函数
     *
     * @param maxAttempts 最大尝试次数
     * @param sleepMillis 重试间隔（毫秒），小于等于 0 时自动设为 1
     */
    public SimpleRetry(int maxAttempts, long sleepMillis) {
        this.maxAttempts = maxAttempts;
        this.sleepMillis = sleepMillis > 0 ? sleepMillis : 1;
    }

    @Override
    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
        Throwable lastThrowable = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return retryCallback.call();
            } catch (Throwable e) {
                Logs.warn(EmojiSymbol.TOOL, "重试 {} 次", attempt, e);
                lastThrowable = e;
                if (sleepMillis > 0 && attempt < maxAttempts) {
                    Threads.sleep(sleepMillis);
                }
            }
        }
        if (lastThrowable == null) {
            lastThrowable = new IOException(StringUtil.format("重试 {} 次，仍然失败", maxAttempts));
        }
        throw new ExtException(EmojiSymbol.TOOL, lastThrowable);
    }
}
