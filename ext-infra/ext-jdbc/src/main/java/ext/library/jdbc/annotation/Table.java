package ext.library.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表名注解
 * <p>
 * 标注在实体类上显式指定表名，未标注时由 {@code JdbcDaoFactory} 按类名首字母小写转下划线推导。
 *
 * @since 4.1.0
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {

    /**
     * 表名
     *
     * @return 表名
     */
    String value();

}
