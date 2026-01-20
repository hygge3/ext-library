package ext.library.tool.holder.function;

import java.io.Serializable;

/**
 * 受检的 Consumer
 * <p>
 * 允许抛出受检异常的消费者函数式接口
 *
 * @param <T> 输入类型
 */
@FunctionalInterface
public interface CheckedConsumer<T> extends Serializable {

    /**
     * 消费输入参数
     *
     * @param t 输入参数
     * @throws Throwable 可能抛出的异常
     */
    void accept(T t) throws Throwable;
}
