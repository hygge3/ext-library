package ext.library.core.datascope;

import java.util.Collections;
import java.util.List;

/**
 * 数据权限范围
 * <p>
 * 由 {@code ext-security} 的拦截器解析后放入 {@link DataScopeContext}，
 * 供 {@code ext-jdbc} 的 DAO 在查询时自动追加过滤条件。
 *
 * @param field  要过滤的列名（可为带表别名的形式，如 {@code u.dept_id}）
 * @param values 允许的值集合；为空表示无数据可见
 * @since 4.1.0
 */
public record DataScopeMeta(String field, List<Object> values) {

    /**
     * 把 null 规范成空集合
     * <p>
     * {@code values()} 会被 {@code addAll} 直接用，Provider 返回 null 是很自然的写法，不能在这里留 NPE。
     */
    public DataScopeMeta {
        values = values == null ? List.of() : List.copyOf(values);
    }

    /**
     * 生成 {@code IN} 条件片段，值一律用 {@code ?} 占位
     * <p>
     * 值为空时返回恒假条件，确保「无数据可见」不会被解析成「全部可见」。
     *
     * @return SQL 条件片段
     */
    public String condition() {
        if (values.isEmpty()) {
            return "1=0";
        }
        String placeholders = String.join(", ", Collections.nCopies(values.size(), "?"));
        return field + " IN (" + placeholders + ")";
    }
}
