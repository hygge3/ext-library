package ext.library.tool.holder;

import java.util.function.Supplier;

/**
 * 延迟加载容器
 * <p>
 * 封装一个 {@link Supplier}，在首次调用 {@link #get()} 时计算值并缓存，
 * 后续调用直接返回缓存值。线程安全。
 *
 * @param <T> 值类型
 * @since 2025.01.01
 */
public final class Lazy<T> implements Supplier<T> {

    /** 值提供者，计算完成后置为 null */
    private volatile Supplier<? extends T> supplier;

    /** 缓存的值 */
    private T value;

    /**
     * 私有构造函数
     *
     * @param supplier 值提供者
     */
    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * 创建延迟加载实例
     *
     * @param supplier 值提供者
     * @param <T>      值类型
     * @return 延迟加载实例
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * 获取值
     * <p>
     * 首次调用时计算值并缓存，后续调用直接返回缓存值。
     *
     * @return 缓存的值
     */
    @Override
    public T get() {
        return (supplier == null) ? value : computeValue();
    }

    /**
     * 计算并缓存值（线程安全）
     *
     * @return 计算后的值
     */
    private synchronized T computeValue() {
        final Supplier<? extends T> s = supplier;
        if (s != null) {
            value = s.get();
            supplier = null;
        }
        return value;
    }
}
