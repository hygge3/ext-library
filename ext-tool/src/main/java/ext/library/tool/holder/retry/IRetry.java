package ext.library.tool.holder.retry;


import ext.library.tool.holder.function.CheckedCallable;

/**
 * 重试接口
 */
public interface IRetry {

    /**
     * Execute the supplied {@link CheckedCallable} with the configured retry semantics.
     * See implementations for configuration details.
     *
     * @param <T>           the return value
     * @param retryCallback the {@link CheckedCallable}
     * @param <E>           the exception thrown
     * @return T the return value
     * @throws E the exception thrown
     */
    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E;

}
