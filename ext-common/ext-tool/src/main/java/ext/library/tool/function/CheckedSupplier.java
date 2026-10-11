package ext.library.tool.function;

import java.io.Serializable;

/**
 * 受检的 Supplier
 * <p>
 * 允许抛出受检异常的提供者函数式接口
 *
 * @param <T> 返回类型
 */
@FunctionalInterface
public interface CheckedSupplier<T> extends Serializable {

    /**
     * 获取结果
     *
     * @return 结果
     *
     * @throws Throwable 可能抛出的异常
     */
    T get() throws Throwable;
}
