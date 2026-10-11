package ext.library.tool.function;

import java.io.Serializable;

/**
 * 受检的 Callable
 * <p>
 * 允许抛出受检异常的可调用函数式接口
 *
 * @param <T> 返回类型
 */
@FunctionalInterface
public interface CheckedCallable<T> extends Serializable {

    /**
     * 执行调用并返回结果
     *
     * @return 执行结果
     *
     * @throws Throwable 可能抛出的异常
     */
    T call() throws Throwable;
}
