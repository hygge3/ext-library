package ext.library.tool.holder;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 单次执行控制器
 * <p>
 * 确保指定的操作只执行一次，线程安全。适用于一次性初始化、单次事件处理等场景。
 *
 * @since 2025.01.01
 */
public final class Once {

    /** 执行状态标记 */
    private final AtomicBoolean executed = new AtomicBoolean(false);

    /**
     * 检查是否可以执行
     * <p>
     * 首次调用返回 {@code true} 并标记为已执行，后续调用返回 {@code false}。
     *
     * @return 如果是首次调用返回 true，否则返回 false
     */
    public boolean canRun() {
        return executed.compareAndSet(false, true);
    }

    /**
     * 执行无参操作（仅首次调用时执行）
     *
     * @param runnable 待执行的操作
     */
    public void run(Runnable runnable) {
        if (canRun()) {
            runnable.run();
        }
    }

    /**
     * 执行单参数操作（仅首次调用时执行）
     *
     * @param consumer 消费者函数
     * @param argument 参数
     * @param <T>      参数类型
     */
    public <T> void run(Consumer<T> consumer, T argument) {
        if (canRun()) {
            consumer.accept(argument);
        }
    }

    /**
     * 执行双参数操作（仅首次调用时执行）
     *
     * @param consumer 双参数消费者函数
     * @param arg1     第一个参数
     * @param arg2     第二个参数
     * @param <T>      第一个参数类型
     * @param <U>      第二个参数类型
     */
    public <T, U> void run(BiConsumer<T, U> consumer, T arg1, U arg2) {
        if (canRun()) {
            consumer.accept(arg1, arg2);
        }
    }

    /**
     * 执行有返回值的操作（仅首次调用时执行）
     *
     * @param function 转换函数
     * @param argument 参数
     * @param <T>      参数类型
     * @param <R>      返回值类型
     * @return 首次调用返回函数执行结果，后续调用返回 null
     */
    public <T, R> R run(Function<T, R> function, T argument) {
        if (canRun()) {
            return function.apply(argument);
        }
        return null;
    }
}
