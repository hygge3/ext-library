package ext.library.tool.holder;

import java.io.Serializable;
import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


/**
 * Holder of a value that is computed lazy.
 */
public class Lazy<T> implements Supplier<T>, Serializable {

    private transient Supplier<? extends T> supplier;

    private T value;

    /**
     * Creates new instance of Lazy.
     *
     * @param supplier Supplier
     * @param <T>      泛型标记
     * @return Lazy
     */
    @NotNull
    @Contract(value = "_->new", pure = true)
    public static <T> Lazy<T> of(final Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    @Contract(pure = true)
    private Lazy(final Supplier<T> supplier) {
        this.supplier = supplier;
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
