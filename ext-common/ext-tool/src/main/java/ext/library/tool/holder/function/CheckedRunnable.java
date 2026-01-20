package ext.library.tool.holder.function;

import java.io.Serializable;

/**
 * 受检的 Runnable
 * <p>
 * 允许抛出受检异常的可执行函数式接口
 */
@FunctionalInterface
public interface CheckedRunnable extends Serializable {

    /**
     * 执行操作
     *
     * @throws Throwable 可能抛出的异常
     */
    void run() throws Throwable;
}
