package ext.library.jdbc;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.util.TypeCastUtil;
import org.springframework.jdbc.core.RowMapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 实体行映射器
 * <p>
 * 通过 JavaBeans 内省拿到 {@link PropertyDescriptor}，逐列读取后经
 * {@link TypeCastUtil#cast(Object, Class)} 转换再 setter 注入。
 * <p>
 * 列名先转驼峰再与属性名匹配，即 {@code create_time} → {@code createTime}；
 * 转不过来的（如本来就是驼峰的列名）直接按原名匹配。
 *
 * @param <T> 实体类型
 * @since 4.1.0
 */
public class EntityRowMapper<T> implements RowMapper<T> {

    private final Class<T> entityType;

    /**
     * 小写属性名 → setter 与其属性类型
     */
    private final Map<String, Property> properties;

    public EntityRowMapper(Class<T> entityType) {
        this.entityType = entityType;
        Map<String, Property> propertyMap = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(entityType);
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                Method setter = descriptor.getWriteMethod();
                if (setter != null) {
                    propertyMap.put(descriptor.getName().toLowerCase(Locale.ROOT),
                            new Property(setter, descriptor.getPropertyType()));
                }
            }
        } catch (IntrospectionException e) {
            throw new ExtException(EmojiSymbol.JDBC, e, "无法内省实体类：{}", entityType.getName());
        }
        this.properties = Map.copyOf(propertyMap);
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T entity;
        try {
            entity = entityType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ExtException(EmojiSymbol.JDBC, e, "实体类必须具备无参构造器：{}", entityType.getName());
        }
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Property property = properties.get(EntityMetaData.toPropertyName(columnName).toLowerCase(Locale.ROOT));
            if (property == null) {
                property = properties.get(columnName.toLowerCase(Locale.ROOT));
            }
            // 结果集里多出的列（如 SELECT * 撞上的扩展字段）直接忽略
            if (property == null) {
                continue;
            }
            Object value = rs.getObject(i);
            if (value == null) {
                continue;
            }
            Object converted = TypeCastUtil.cast(value, property.type());
            try {
                property.setter().invoke(entity, converted);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ExtException(EmojiSymbol.JDBC, e, "属性赋值失败：{}.{}", entityType.getName(),
                        property.setter().getName());
            }
        }
        return entity;
    }

    /**
     * 属性与其写入方法，避免为类型再单独维护一张映射表
     *
     * @param setter 属性写入方法
     * @param type   属性类型
     */
    private record Property(Method setter, Class<?> type) {
    }

}
