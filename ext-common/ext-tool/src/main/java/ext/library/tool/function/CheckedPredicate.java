package ext.library.tool.function;

import java.io.Serializable;

/**
 * 受检的 Predicate
 * <p>
 * 允许抛出受检异常的断言函数式接口
 *
 * @param <T> 输入类型
 */
@FunctionalInterface
public interface CheckedPredicate<T> extends Serializable {

    /**
     * 对给定参数进行断言判断
     *
     * @param t 输入参数
     *
     * @return 如果输入参数匹配断言返回 true，否则返回 false
     *
     * @throws Throwable 可能抛出的异常
     */
    boolean test(T t) throws Throwable;
}
