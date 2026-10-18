package ext.library.jdbc.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 主键注解
 * <p>
 * 标注在实体类的字段上标识主键，未标注时由 {@code JdbcDaoFactory} 退化为查找名为 {@code id} 的字段。
 *
 * @since 4.1.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {

}
