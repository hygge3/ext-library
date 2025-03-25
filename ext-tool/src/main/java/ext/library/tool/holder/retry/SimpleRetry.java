package ext.library.tool.holder.retry;

import ext.library.tool.$;
import ext.library.tool.core.Exceptions;
import ext.library.tool.core.Threads;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 简单的 retry 重试
 */
@Getter
@Slf4j
public final class SimpleRetry implements IRetry {

    /**
     * The default limit to the number of attempts for a new policy.
     */
    static final int DEFAULT_MAX_ATTEMPTS = 3;

    /**
     * Default back off period - 1ms.
     */
    static final long DEFAULT_BACK_OFF_PERIOD = 1L;

    /**
     * 重试次数
     */
    final int maxAttempts;

    /**
     * 重试时间间隔
     */
    final long sleepMillis;

    public SimpleRetry() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_BACK_OFF_PERIOD);
    }

    public SimpleRetry(int maxAttempts) {
        this(maxAttempts, DEFAULT_BACK_OFF_PERIOD);
    }

    public SimpleRetry(int maxAttempts, long sleepMillis) {
        this.maxAttempts = maxAttempts;
        this.sleepMillis = (sleepMillis > 0 ? sleepMillis : 1);
    }

    @Override
    public <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E {
        int retryCount;
        Throwable lastThrowable = null;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                return retryCallback.call();
            } catch (Throwable e) {
                retryCount = i + 1;
                log.warn("[🛠️] retry on {} times error{}.", retryCount, e.getMessage());
                lastThrowable = e;
                if (sleepMillis > 0 && retryCount < maxAttempts) {
                    Threads.sleep(sleepMillis);
                }
            }
        }
        if (lastThrowable == null) {
            lastThrowable = new IOException($.format("retry on {} times,still fail.", maxAttempts));
        }
        throw Exceptions.unchecked(lastThrowable);
    }

}
