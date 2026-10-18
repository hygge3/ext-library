package ext.library.jdbc;

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
        StringBuilder sb = new StringBuilder(columnName.length());
        boolean upperNext = false;
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (c == '_') {
                upperNext = true;
            } else if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
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
        return toUnderscore(propertyName);
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
        return toUnderscore(entityClass.getSimpleName());
    }

    /**
     * 驼峰转下划线
     */
    private static String toUnderscore(String name) {
        StringBuilder sb = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
