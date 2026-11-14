package ext.library.jdbc;

import ext.library.core.datascope.DataScopeContext;
import ext.library.core.datascope.DataScopeMeta;
import ext.library.core.util.BeanUtil;
import ext.library.servlet.response.PageQuery;
import ext.library.servlet.response.PageResult;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 通用 DAO
 * <p>
 * 基于 {@link JdbcClient} 实现基础 CRUD 与条件查询，所有值均以 {@code ?} 占位符绑定，
 * 不会拼接到 SQL 文本中。
 *
 * <pre>{@code
 * JdbcDao<User> userDao = jdbcDaoFactory.of(User.class);
 * Optional<User> user = userDao.selectById(1L);
 * }</pre>
 *
 * @param <T> 实体类型
 * @since 4.1.0
 */
public class JdbcDao<T> {

    private final JdbcClient jdbcClient;

    private final EntityMetaData<T> metaData;

    private final EntityRowMapper<T> rowMapper;

    public JdbcDao(JdbcClient jdbcClient, EntityMetaData<T> metaData) {
        this.jdbcClient = jdbcClient;
        this.metaData = metaData;
        this.rowMapper = new EntityRowMapper<>(metaData.entityType());
        Logs.debug(EmojiSymbol.JDBC, "初始化 DAO：table={}, entity={}", metaData.tableName(),
                metaData.entityType().getSimpleName());
    }

    /**
     * 获取实体元数据
     *
     * @return 实体元数据
     */
    public EntityMetaData<T> metaData() {
        return metaData;
    }

    // region 查询

    /**
     * 按主键查询
     *
     * @param id 主键值
     *
     * @return 实体，不存在时为空
     */
    public Optional<T> selectById(Object id) {
        // 数据权限必须盖住单条读取，否则拿到 ID 就能跨部门读
        List<Object> params = new ArrayList<>();
        String where = applyDataScope(metaData.idColumn() + " = ?", List.of(id), params);
        String sql = "SELECT * FROM " + metaData.tableName() + whereFragment(where);
        return jdbcClient.sql(sql).params(params).query(rowMapper).optional();
    }

    /**
     * 条件查询列表
     *
     * @param wrapper 查询条件
     *
     * @return 实体列表
     */
    public List<T> selectList(QueryWrapper<T> wrapper) {
        List<Object> params = new ArrayList<>();
        String where = applyDataScope(wrapper.whereClause(), wrapper.whereParameters(), params);
        params.addAll(wrapper.havingParameters());
        params.addAll(wrapper.limitParameters());

        String sql = "SELECT * FROM " + metaData.tableName()
                + whereFragment(where)
                + wrapper.groupByHavingClause()
                + wrapper.orderByClause()
                + wrapper.limitClause()
                + wrapper.lastClause();
        return jdbcClient.sql(sql).params(params).query(rowMapper).list();
    }

    /**
     * 条件查询总数
     *
     * @param wrapper 查询条件
     *
     * @return 总数，无记录时返回 0
     */
    public long selectCount(QueryWrapper<T> wrapper) {
        List<Object> params = new ArrayList<>();
        String where = applyDataScope(wrapper.whereClause(), wrapper.whereParameters(), params);
        params.addAll(wrapper.havingParameters());

        String sql = "SELECT COUNT(*) FROM " + metaData.tableName()
                + whereFragment(where)
                + wrapper.groupByHavingClause();
        return jdbcClient.sql(sql).params(params).query(Long.class).optional().orElse(0L);
    }

    /**
     * 分页查询
     *
     * @param query   分页参数，总数会回填到该对象
     * @param wrapper 查询条件
     *
     * @return 分页结果
     */
    public PageResult<T> selectPage(PageQuery query, QueryWrapper<T> wrapper) {
        long size = Math.max(query.getSize(), 1);
        long total = selectCount(wrapper);
        query.setTotal(total);
        if (total == 0) {
            return PageResult.empty(query);
        }
        wrapper.limit((int) query.offset(), (int) size);
        List<T> records = selectList(wrapper);
        long pages = (long) Math.ceil((double) total / size);
        Logs.debug(EmojiSymbol.JDBC, "分页查询：table={}, page={}, size={}, total={}", metaData.tableName(),
                query.getPage(), size, total);
        return new PageResult<>(query.getPage(), size, total, pages, records);
    }

    // endregion

    // region 写入

    /**
     * 插入实体
     *
     * @param entity 实体，主键为空时由数据库自增
     *
     * @return 影响行数
     */
    public int insert(T entity) {
        Map<String, Object> values = BeanUtil.beanToMap(entity);
        // 主键为空时把 id 列整个从 INSERT 里去掉，交给数据库自增；
        // 带上它只会绑一个 null 进去，PG 直接报 not-null 违反
        String[] properties = Arrays.stream(metaData.properties())
                .filter(property -> values.get(property) != null || !property.equals(metaData.idField()))
                .toArray(String[]::new);
        List<String> columns = Arrays.stream(properties).map(EntityMetaData::toColumnName).toList();
        String placeholders = placeholders(columns.size());
        String sql = "INSERT INTO " + metaData.tableName()
                + " (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";
        Logs.debug(EmojiSymbol.JDBC, "插入实体：table={}", metaData.tableName());
        return jdbcClient.sql(sql).params(readValues(values, properties)).update();
    }

    /**
     * 按主键更新实体全部列
     * <p>
     * 主键列不进 {@code SET} 列表。
     *
     * @param entity 实体
     *
     * @return 影响行数
     */
    public int update(T entity) {
        String[] settable = settableProperties();
        String sql = "UPDATE " + metaData.tableName()
                + " SET " + assignments(settable)
                + " WHERE " + metaData.idColumn() + " = ?";
        Map<String, Object> values = BeanUtil.beanToMap(entity);
        List<Object> params = new ArrayList<>(readValues(values, settable));
        params.add(values.get(metaData.idField()));
        Logs.debug(EmojiSymbol.JDBC, "更新实体：table={}", metaData.tableName());
        return jdbcClient.sql(sql).params(params).update();
    }

    /**
     * 按附加条件更新实体全部列
     * <p>
     * 附加条件为空时回退为按主键更新；实体主键为 null 且附加条件为空时直接拒绝，避免全表覆盖。
     *
     * @param entity  实体
     * @param wrapper 附加条件
     *
     * @return 影响行数
     */
    public int update(T entity, QueryWrapper<T> wrapper) {
        String where = wrapper.whereClause();
        Map<String, Object> values = BeanUtil.beanToMap(entity);
        Object id = values.get(metaData.idField());
        if (where.isEmpty() && id == null) {
            throw new ExtException(EmojiSymbol.JDBC, "拒绝执行全表更新：条件为空且主键为 null");
        }

        String[] settable = settableProperties();
        List<Object> params = new ArrayList<>(readValues(values, settable));
        where = where.isEmpty()
                ? applyDataScope(metaData.idColumn() + " = ?", List.of(id), params)
                : applyDataScope(where, wrapper.whereParameters(), params);

        String sql = "UPDATE " + metaData.tableName()
                + " SET " + assignments(settable)
                + whereFragment(where);

        Logs.debug(EmojiSymbol.JDBC, "按条件更新实体：table={}", metaData.tableName());
        return jdbcClient.sql(sql).params(params).update();
    }

    /**
     * 按主键删除
     *
     * @param id 主键值
     *
     * @return 影响行数
     */
    public int deleteById(Object id) {
        // 同 selectById：只靠主键过滤等于没有数据权限
        List<Object> params = new ArrayList<>();
        String where = applyDataScope(metaData.idColumn() + " = ?", List.of(id), params);
        String sql = "DELETE FROM " + metaData.tableName() + whereFragment(where);
        Logs.debug(EmojiSymbol.JDBC, "按主键删除：table={}, id={}", metaData.tableName(), id);
        return jdbcClient.sql(sql).params(params).update();
    }

    /**
     * 按条件删除
     *
     * @param wrapper 查询条件，条件为空时直接拒绝，避免全表删除
     *
     * @return 影响行数
     */
    public int delete(QueryWrapper<T> wrapper) {
        List<Object> params = new ArrayList<>();
        String where = applyDataScope(wrapper.whereClause(), wrapper.whereParameters(), params);
        if (where.isEmpty()) {
            throw new ExtException(EmojiSymbol.JDBC, "拒绝执行全表删除：条件为空");
        }
        String sql = "DELETE FROM " + metaData.tableName() + whereFragment(where);
        Logs.debug(EmojiSymbol.JDBC, "按条件删除：table={}", metaData.tableName());
        return jdbcClient.sql(sql).params(params).update();
    }

    // endregion

    // region 私有辅助

    /**
     * 可参与 {@code SET} 的属性，即排除主键后的属性列表
     */
    private String[] settableProperties() {
        return Arrays.stream(metaData.properties())
                .filter(p -> !p.equals(metaData.idField()))
                .toArray(String[]::new);
    }

    /**
     * 生成 {@code SET col = ?, col = ?} 片段
     */
    private static String assignments(String[] properties) {
        if (properties.length == 0) {
            throw new ExtException(EmojiSymbol.JDBC, "实体除主键外没有任何可写属性");
        }
        return Arrays.stream(properties)
                .map(p -> EntityMetaData.toColumnName(p) + " = ?")
                .collect(Collectors.joining(", "));
    }

    /**
     * 条件片段转 {@code WHERE} 子句，无条件时返回空串
     */
    private static String whereFragment(String where) {
        return where.isEmpty() ? "" : " WHERE " + where;
    }

    /**
     * 拼接数据权限范围条件
     * <p>
     * 数据权限由 {@code ext-security} 的拦截器写入 {@link DataScopeContext}，
     * 此处把它追加到查询条件上；未设置时不做任何改变。
     *
     * @param where      已有条件片段
     * @param whereParams 已有条件的绑定参数
         * @param params     出参，按 SQL 中 {@code ?} 的顺序追加绑定值
     *
     * @return 追加数据权限后的条件片段
     */
    private static String applyDataScope(String where, List<Object> whereParams, List<Object> params) {
        params.addAll(whereParams);
        DataScopeMeta scope = DataScopeContext.get();
        if (scope == null) {
            return where;
        }
        params.addAll(scope.values());
        return where.isEmpty() ? scope.condition() : where + " AND " + scope.condition();
    }

    private static String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    /**
     * 按给定属性顺序读取实体属性值
     *
     * @param values     实体属性快照
     * @param properties 属性名列表
     *
     * @return 与属性顺序一致的值列表
     */
    private static List<Object> readValues(Map<String, Object> values, String[] properties) {
        return Arrays.stream(properties).map(values::get).toList();
    }

    // endregion

}
