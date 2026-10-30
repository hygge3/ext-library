package ext.library.jdbc;

import ext.library.tool.util.StringUtil;

/**
 * 实体元数据
 * <p>
 * 描述实体类与数据库表的对应关系，包括表名、主键字段以及参与读写的属性列表。
 *
 * @param entityType 实体类型
 * @param tableName  表名
 * @param idField    主键属性名（驼峰形式）
 * @param properties 参与读写的属性名（驼峰形式，不含 {@code class}）
 * @param <T>        实体类型泛型
 * @since 4.1.0
 */
public record EntityMetaData<T>(Class<T> entityType, String tableName, String idField, String[] properties) {

    /**
     * 获取主键列名
     *
     * @return 下划线形式的主键列名
     */
    public String idColumn() {
        return toColumnName(idField);
    }

    /**
     * 列名转属性名
     * <p>
     * {@code create_time} → {@code createTime}；已是驼峰形式则原样返回。
     *
     * @param columnName 列名
     *
     * @return 驼峰形式的属性名
     */
    public static String toPropertyName(String columnName) {
        return StringUtil.underlineToCamelCase(columnName);
    }

    /**
     * 属性名转列名
     * <p>
     * {@code createTime} → {@code create_time}。
     *
     * @param propertyName 属性名
     *
     * @return 下划线形式的列名
     */
    public static String toColumnName(String propertyName) {
        return StringUtil.camelToUnderline(propertyName);
    }

    /**
     * 类名转表名
     * <p>
     * {@code UserInfo} → {@code user_info}。
     *
     * @param entityClass 实体类型
     *
     * @return 下划线形式的表名
     */
    public static String toTableName(Class<?> entityClass) {
        return StringUtil.camelToUnderline(entityClass.getSimpleName());
    }

}
