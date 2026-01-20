package ext.library.tool.holder;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.holder.function.CheckedCallable;
import ext.library.tool.holder.function.CheckedComparator;
import ext.library.tool.holder.function.CheckedConsumer;
import ext.library.tool.holder.function.CheckedFunction;
import ext.library.tool.holder.function.CheckedPredicate;
import ext.library.tool.holder.function.CheckedRunnable;
import ext.library.tool.holder.function.CheckedSupplier;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Lambda 受检异常处理工具
 * <p>
 * 将可能抛出受检异常的函数式接口包装为标准函数式接口，
 * 异常会被转换为 {@link ExtException} 抛出。
 * <p>
 * 参考：
 * <ul>
 *   <li><a href="https://segmentfault.com/a/1190000007832130">当 Lambda 遇上受检异常</a></li>
 *   <li><a href="https://github.com/jOOQ/jOOL">jOOλ</a></li>
 * </ul>
 *
 * @since 2025.01.01
 */
public final class Unchecked {

    private Unchecked() {
    }

    /**
     * 包装可能抛出异常的 Function
     *
     * @param function 受检函数
     * @param <T>      输入类型
     * @param <R>      返回类型
     * @return 包装后的 Function
     */
    public static <T, R> Function<T, R> function(CheckedFunction<T, R> function) {
        Objects.requireNonNull(function);
        return t -> {
            try {
                return function.apply(t);
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }

    /**
     * 包装可能抛出异常的 Consumer
     *
     * @param consumer 受检消费者
     * @param <T>      输入类型
     * @return 包装后的 Consumer
     */
    public static <T> Consumer<T> consumer(CheckedConsumer<T> consumer) {
        Objects.requireNonNull(consumer);
        return t -> {
            try {
                consumer.accept(t);
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }

    /**
     * 包装可能抛出异常的 Supplier
     *
     * @param supplier 受检提供者
     * @param <T>      返回类型
     * @return 包装后的 Supplier
     */
    public static <T> Supplier<T> supplier(CheckedSupplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }

    /**
     * 包装可能抛出异常的 Predicate
     *
     * @param predicate 受检断言
     * @param <T>       输入类型
     * @return 包装后的 Predicate
     */
    public static <T> Predicate<T> predicate(CheckedPredicate<T> predicate) {
        Objects.requireNonNull(predicate);
        return t -> {
            try {
                return predicate.test(t);
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }

    /**
     * 包装可能抛出异常的 Runnable
     *
     * @param runnable 受检可执行对象
     * @return 包装后的 Runnable
     */
    public static Runnable runnable(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable);
        return () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }

    /**
     * 包装可能抛出异常的 Callable
     *
     * @param callable 受检可调用对象
     * @param <T>      返回类型
     * @return 包装后的 Callable
     */
    public static <T> Callable<T> callable(CheckedCallable<T> callable) {
        Objects.requireNonNull(callable);
        return () -> {
            try {
                return callable.call();
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }

    /**
     * 包装可能抛出异常的 Comparator
     *
     * @param comparator 受检比较器
     * @param <T>        比较对象类型
     * @return 包装后的 Comparator
     */
    public static <T> Comparator<T> comparator(CheckedComparator<T> comparator) {
        Objects.requireNonNull(comparator);
        return (o1, o2) -> {
            try {
                return comparator.compare(o1, o2);
            } catch (Throwable e) {
                throw new ExtException(EmojiSymbol.TOOL, e);
            }
        };
    }
}
