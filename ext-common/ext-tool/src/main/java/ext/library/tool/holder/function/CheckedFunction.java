package ext.library.tool.holder.function;

import java.io.Serializable;

/**
 * 受检的 Function
 * <p>
 * 允许抛出受检异常的函数式接口
 *
 * @param <T> 输入类型
 * @param <R> 返回类型
 */
@FunctionalInterface
public interface CheckedFunction<T, R> extends Serializable {

    /**
     * 执行函数
     *
     * @param t 输入参数
     * @return 返回结果
     * @throws Throwable 可能抛出的异常
     */
    R apply(T t) throws Throwable;
}
