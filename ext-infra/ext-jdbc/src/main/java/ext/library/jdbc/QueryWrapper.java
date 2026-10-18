package ext.library.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 链式查询条件构造器
 * <p>
 * 所有条件值一律使用 {@code ?} 占位符 + 参数绑定，绝不做字符串拼接，从根本上避免 SQL 注入。
 *
 * <pre>{@code
 * List<User> users = dao.selectList(QueryWrapper.<User>of()
 *         .eq("status", 1)
 *         .like("name", "%张%")
 *         .in("id", List.of(1L, 2L, 3L))
 *         .orderByDesc("create_time")
 *         .limit(0, 10));
 * }</pre>
 *
 * <p>
 * 本类只负责产出条件片段与对应参数，不拼接 {@code SELECT} 等语句骨架，
 * 骨架由 {@link JdbcDao} 依据操作类型组装。
 *
 * @param <T> 实体类型
 * @since 4.1.0
 */
public class QueryWrapper<T> {

    /**
     * WHERE 条件片段，首条不带连接词
     */
    private final List<String> conditions = new ArrayList<>();

    /**
     * WHERE 绑定参数，顺序与 {@code ?} 一一对应
     */
    private final List<Object> conditionParams = new ArrayList<>();

    /**
     * GROUP BY 字段
     */
    private final List<String> groupByColumns = new ArrayList<>();

    /**
     * HAVING 条件片段，首条不带连接词
     */
    private final List<String> havingConditions = new ArrayList<>();

    /**
     * HAVING 绑定参数
     */
    private final List<Object> havingParams = new ArrayList<>();

    /**
     * ORDER BY 片段
     */
    private final List<String> orderByItems = new ArrayList<>();

    /**
     * 偏移量
     */
    private Integer offset;

    /**
     * 返回条数
     */
    private Integer limit;

    /**
     * 追加的裸 SQL
     */
    private String lastSql;

    /**
     * 创建一个空条件构造器
     *
     * @param <T> 实体类型泛型
     *
     * @return 条件构造器
     */
    public static <T> QueryWrapper<T> of() {
        return new QueryWrapper<>();
    }

    // region 条件

    /**
     * 追加 AND 条件
     * <p>
     * {@code condition} 中的值必须写成 {@code ?} 占位符，真实值通过 {@code args} 绑定。
     *
     * @param condition 条件片段，如 {@code "status = ?"}
     * @param args      绑定参数
     *
     * @return this
     */
    public QueryWrapper<T> where(String condition, Object... args) {
        return and(condition, args);
    }

    /**
     * 追加 AND 条件
     *
     * @param condition 条件片段，如 {@code "status = ?"}
     * @param args      绑定参数
     *
     * @return this
     */
    public QueryWrapper<T> and(String condition, Object... args) {
        conditions.add(conditions.isEmpty() ? condition : "AND " + condition);
        conditionParams.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * 追加 OR 条件
     *
     * @param condition 条件片段，如 {@code "status = ?"}
     * @param args      绑定参数
     *
     * @return this
     */
    public QueryWrapper<T> or(String condition, Object... args) {
        conditions.add(conditions.isEmpty() ? condition : "OR " + condition);
        conditionParams.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * 等值条件
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> eq(String column, Object value) {
        return and(column + " = ?", value);
    }

    /**
     * 不等条件
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> ne(String column, Object value) {
        return and(column + " <> ?", value);
    }

    /**
     * 小于条件
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> lt(String column, Object value) {
        return and(column + " < ?", value);
    }

    /**
     * 小于等于条件
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> le(String column, Object value) {
        return and(column + " <= ?", value);
    }

    /**
     * 大于条件
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> gt(String column, Object value) {
        return and(column + " > ?", value);
    }

    /**
     * 大于等于条件
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> ge(String column, Object value) {
        return and(column + " >= ?", value);
    }

    /**
     * IN 条件
     * <p>
     * 生成 {@code column IN (?, ?, ...)}；集合为空时生成恒假条件 {@code 1 = 0}。
     *
     * @param column 列名
     * @param values 值集合
     *
     * @return this
     */
    public QueryWrapper<T> in(String column, Collection<?> values) {
        if (values.isEmpty()) {
            return and("1 = 0");
        }
        return and(column + " IN (" + placeholders(values.size()) + ")", values.toArray());
    }

    /**
     * NOT IN 条件
     * <p>
     * 生成 {@code column NOT IN (?, ?, ...)}；集合为空时生成恒真条件 {@code 1 = 1}。
     *
     * @param column 列名
     * @param values 值集合
     *
     * @return this
     */
    public QueryWrapper<T> notIn(String column, Collection<?> values) {
        if (values.isEmpty()) {
            return and("1 = 1");
        }
        return and(column + " NOT IN (" + placeholders(values.size()) + ")", values.toArray());
    }

    /**
     * BETWEEN 条件
     *
     * @param column 列名
     * @param start  起始值（含）
     * @param end    结束值（含）
     *
     * @return this
     */
    public QueryWrapper<T> between(String column, Object start, Object end) {
        return and(column + " BETWEEN ? AND ?", start, end);
    }

    /**
     * IS NULL 条件
     *
     * @param column 列名
     *
     * @return this
     */
    public QueryWrapper<T> isNull(String column) {
        return and(column + " IS NULL");
    }

    /**
     * IS NOT NULL 条件
     *
     * @param column 列名
     *
     * @return this
     */
    public QueryWrapper<T> isNotNull(String column) {
        return and(column + " IS NOT NULL");
    }

    /**
     * LIKE 条件
     * <p>
     * 生成 {@code column LIKE ?}，模糊匹配请自行传入 {@code %关键字%}。
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> like(String column, Object value) {
        return and(column + " LIKE ?", value);
    }

    /**
     * LIKE 条件，值自动包裹 {@code %}
     *
     * @param column 列名
     * @param value  值
     *
     * @return this
     */
    public QueryWrapper<T> likeAuto(String column, String value) {
        return like(column, "%" + value + "%");
    }

    // endregion

    // region 聚合与排序

    /**
     * GROUP BY
     *
     * @param columns 字段
     *
     * @return this
     */
    public QueryWrapper<T> groupBy(String... columns) {
        groupByColumns.addAll(Arrays.asList(columns));
        return this;
    }

    /**
     * HAVING 条件
     *
     * @param condition 条件片段，值必须写成 {@code ?} 占位符
     * @param args      绑定参数
     *
     * @return this
     */
    public QueryWrapper<T> having(String condition, Object... args) {
        havingConditions.add(havingConditions.isEmpty() ? condition : "AND " + condition);
        havingParams.addAll(Arrays.asList(args));
        return this;
    }

    /**
     * ORDER BY 升序
     *
     * @param columns 字段
     *
     * @return this
     */
    public QueryWrapper<T> orderByAsc(String... columns) {
        orderByItems.addAll(Arrays.stream(columns).map(c -> c + " ASC").toList());
        return this;
    }

    /**
     * ORDER BY 降序
     *
     * @param columns 字段
     *
     * @return this
     */
    public QueryWrapper<T> orderByDesc(String... columns) {
        orderByItems.addAll(Arrays.stream(columns).map(c -> c + " DESC").toList());
        return this;
    }

    /**
     * 分页
     *
     * @param offset 偏移量，从 0 开始
     * @param size   返回条数
     *
     * @return this
     */
    public QueryWrapper<T> limit(int offset, int size) {
        this.offset = offset;
        this.limit = size;
        return this;
    }

    /**
     * 限制条数
     *
     * @param size 返回条数
     *
     * @return this
     */
    public QueryWrapper<T> limit(int size) {
        this.limit = size;
        return this;
    }

    /**
     * 追加裸 SQL
     * <p>
     * <b>警告：</b>该方法直接拼接字符串，是本类唯一的 SQL 注入风险点，仅供完全可信的场景使用，
     * 任何来自用户输入的内容都必须使用 {@code ?} 占位符方法。
     *
     * @param rawSql 裸 SQL 片段
     *
     * @return this
     */
    public QueryWrapper<T> last(String rawSql) {
        this.lastSql = rawSql;
        return this;
    }

    // endregion

    // region 片段产出

    /**
     * WHERE 条件片段，不含 {@code WHERE} 前缀
     *
     * @return 条件片段，无条件时返回空串
     */
    public String whereClause() {
        return String.join(" ", conditions);
    }

    /**
     * WHERE 条件绑定参数
     *
     * @return 参数列表
     */
    public List<Object> whereParameters() {
        return Collections.unmodifiableList(conditionParams);
    }

    /**
     * GROUP BY + HAVING 片段，COUNT 查询同样需要
     *
     * @return 片段，无内容时返回空串
     */
    public String groupByHavingClause() {
        if (groupByColumns.isEmpty() && havingConditions.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (!groupByColumns.isEmpty()) {
            sb.append(" GROUP BY ").append(String.join(", ", groupByColumns));
        }
        if (!havingConditions.isEmpty()) {
            sb.append(" HAVING ").append(String.join(" ", havingConditions));
        }
        return sb.toString();
    }

    /**
     * HAVING 绑定参数
     *
     * @return 参数列表
     */
    public List<Object> havingParameters() {
        return Collections.unmodifiableList(havingParams);
    }

    /**
     * ORDER BY 片段
     *
     * @return 片段，未设置排序时返回空串
     */
    public String orderByClause() {
        return orderByItems.isEmpty() ? "" : " ORDER BY " + String.join(", ", orderByItems);
    }

    /**
     * LIMIT / OFFSET 片段
     *
     * @return 片段，未设置分页时返回空串
     */
    public String limitClause() {
        StringBuilder sb = new StringBuilder();
        if (limit != null) {
            sb.append(" LIMIT ?");
        }
        if (offset != null) {
            sb.append(" OFFSET ?");
        }
        return sb.toString();
    }

    /**
     * 分页绑定参数，顺序与 {@link #limitClause()} 一致
     *
     * @return 参数列表，未设置分页时返回空列表
     */
    public List<Object> limitParameters() {
        if (limit == null && offset == null) {
            return Collections.emptyList();
        }
        List<Object> params = new ArrayList<>(2);
        if (limit != null) {
            params.add(limit);
        }
        if (offset != null) {
            params.add(offset);
        }
        return params;
    }

    /**
     * 尾部裸 SQL 片段
     *
     * @return 片段，未设置时返回空串
     */
    public String lastClause() {
        return lastSql == null ? "" : " " + lastSql;
    }

    // endregion

    /**
     * 生成与数量匹配的占位符
     */
    private static String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

}
