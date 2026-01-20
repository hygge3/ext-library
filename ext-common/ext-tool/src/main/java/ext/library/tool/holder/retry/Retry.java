package ext.library.tool.holder.retry;

/**
 * 重试接口
 * <p>
 * 定义重试操作的执行语义，具体重试策略由实现类决定。
 *
 * @since 2025.01.01
 */
public interface Retry {

    /**
     * 执行带重试语义的回调操作
     * <p>
     * 具体的重试次数、间隔等配置由实现类决定。
     *
     * @param retryCallback 重试回调
     * @param <T>           返回值类型
     * @param <E>           可能抛出的异常类型
     * @return 回调执行结果
     * @throws E 当所有重试都失败时抛出的异常
     */
    <T, E extends Throwable> T execute(RetryCallback<T, E> retryCallback) throws E;
}
