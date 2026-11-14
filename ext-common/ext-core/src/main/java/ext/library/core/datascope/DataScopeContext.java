package ext.library.core.datascope;

import org.jspecify.annotations.Nullable;

/**
 * 数据权限上下文
 * <p>
 * 线程级持有当前请求的数据权限范围，请求结束必须清空。
 *
 * @since 4.1.0
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScopeMeta> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    /**
     * 设置当前线程的数据权限范围
     *
     * @param meta 数据权限范围
     */
    public static void set(DataScopeMeta meta) {
        HOLDER.set(meta);
    }

    /**
     * 获取当前线程的数据权限范围
     *
     * @return 数据权限范围，未设置时为 {@code null}
     */
    public static @Nullable DataScopeMeta get() {
        return HOLDER.get();
    }

    /**
     * 清除当前线程的数据权限范围
     */
    public static void clear() {
        HOLDER.remove();
    }
}
