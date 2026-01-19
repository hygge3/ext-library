package ext.library.tool.holder;

import java.util.function.Supplier;


/**
 * 懒加载
 */
public final class Lazy<T> implements Supplier<T> {

    private transient Supplier<? extends T> supplier;

    private T value;

    private Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Creates new instance of Lazy.
     *
     * @param supplier Supplier
     * @param <T>      泛型标记
     *
     * @return Lazy
     */
    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Returns the value. Value will be computed on first call.
     *
     * @return lazy value
     */
    @Override
    public T get() {
        return (supplier == null) ? value : computeValue();
    }

    private synchronized T computeValue() {
        final Supplier<? extends T> s = supplier;
        if (s != null) {
            value = s.get();
            supplier = null;
        }
        return value;
    }

}