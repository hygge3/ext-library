package ext.library.jdbc;

import ext.library.jdbc.annotation.Id;
import ext.library.jdbc.annotation.Table;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 DAO 工厂
 * <p>
 * 基于 {@link JdbcClient} 为实体类生成轻量 DAO，无需编写实现类。
 *
 * <pre>{@code
 * JdbcDao<User> userDao = jdbcDaoFactory.of(User.class);
 * }</pre>
 *
 * @since 4.1.0
 */
public class JdbcDaoFactory {

    private final JdbcClient jdbcClient;

    /**
     * 实体元数据缓存
     */
    private final Map<Class<?>, EntityMetaData<?>> metaDataCache = new ConcurrentHashMap<>();

    public JdbcDaoFactory(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * 创建实体对应的 DAO
     *
     * @param entityClass 实体类型
     * @param <T>         实体类型泛型
     *
     * @return DAO 实例
     */
    public <T> JdbcDao<T> of(Class<T> entityClass) {
        return new JdbcDao<>(jdbcClient, metaData(entityClass));
    }

    /**
     * 解析实体元数据，结果缓存
     *
     * @param entityClass 实体类型
     * @param <T>         实体类型泛型
     *
     * @return 实体元数据
     */
    @SuppressWarnings("unchecked")
    private <T> EntityMetaData<T> metaData(Class<T> entityClass) {
        return (EntityMetaData<T>) metaDataCache.computeIfAbsent(entityClass, JdbcDaoFactory::parseMetaData);
    }

    /**
     * 解析实体元数据
     * <p>
     * 表名优先取 {@link Table} 注解值，否则按类名转下划线；
     * 主键优先取 {@link Id} 标注字段，其次取名为 {@code id} 的字段，都没有则抛异常；
     * 属性列表取 JavaBeans 规范中具备读写方法的属性，因此不要求实体有无参构造器。
     *
     * @param entityClass 实体类型
     * @param <T>         实体类型泛型
     *
     * @return 实体元数据
     */
    private static <T> EntityMetaData<T> parseMetaData(Class<T> entityClass) {
        if (entityClass.isInterface() || Modifier.isAbstract(entityClass.getModifiers())) {
            throw new ExtException(EmojiSymbol.JDBC, "实体类必须是具体类：{}", entityClass.getName());
        }
        Table table = entityClass.getAnnotation(Table.class);
        String tableName = table != null ? table.value() : EntityMetaData.toTableName(entityClass);
        String[] properties = readWriteProperties(entityClass);
        return new EntityMetaData<>(entityClass, tableName, resolveIdField(entityClass, properties), properties);
    }

    /**
     * 探测主键字段
     */
    private static String resolveIdField(Class<?> entityClass, String[] properties) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }
        if (Arrays.asList(properties).contains("id")) {
            return "id";
        }
        throw new ExtException(EmojiSymbol.JDBC, "实体类未找到主键：请标注 @Id 或提供 id 字段：{}", entityClass.getName());
    }

    /**
     * 枚举具备读写方法的属性，内省结果本身已排除 {@code class}
     */
    private static String[] readWriteProperties(Class<?> entityClass) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entityClass);
            List<String> names = Arrays.stream(beanInfo.getPropertyDescriptors())
                    .filter(pd -> pd.getReadMethod() != null && pd.getWriteMethod() != null)
                    .map(PropertyDescriptor::getName)
                    .toList();
            if (names.isEmpty()) {
                throw new ExtException(EmojiSymbol.JDBC, "实体类没有可读写的属性：{}", entityClass.getName());
            }
            return names.toArray(new String[0]);
        } catch (IntrospectionException e) {
            throw new ExtException(EmojiSymbol.JDBC, e, "无法内省实体类：{}", entityClass.getName());
        }
    }

}
