package ext.library.tool.holder.function;

import java.io.Serializable;

/**
 * 受检的 Comparator
 * <p>
 * 允许抛出受检异常的比较器函数式接口
 *
 * @param <T> 比较对象类型
 */
@FunctionalInterface
public interface CheckedComparator<T> extends Serializable {

    /**
     * 比较两个对象的顺序
     *
     * @param o1 第一个对象
     * @param o2 第二个对象
     * @return 负数表示 o1 &lt; o2，零表示相等，正数表示 o1 &gt; o2
     * @throws Throwable 可能抛出的异常
     */
    int compare(T o1, T o2) throws Throwable;
}
