package ext.library.tool.holder.retry;

import java.io.Serial;
import java.io.Serializable;

/**
 * 重试回调接口
 * <p>
 * 封装可重试的操作，操作应当是幂等的，以确保多次执行的结果一致。
 *
 * @param <T> 返回值类型
 * @param <E> 可能抛出的异常类型
 * @since 2025.01.01
 */
@FunctionalInterface
public interface RetryCallback<T, E extends Throwable> extends Serializable {

    @Serial
    long serialVersionUID = 1L;

    /**
     * 执行可重试的操作
     * <p>
     * 操作应当是幂等的，但实现可以在重试时选择执行补偿逻辑。
     *
     * @return 操作执行结果
     * @throws E 操作失败时抛出的异常
     */
    T call() throws E;
}
