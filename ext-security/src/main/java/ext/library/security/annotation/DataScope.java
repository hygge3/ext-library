package ext.library.security.annotation;

import ext.library.security.enums.DataScopeType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解
 * <p>
 * 标注在 Controller 方法或类上，声明该接口查询的数据范围。
 * 拦截器解析后写入 {@code DataScopeContext}，由 {@code ext-jdbc} 的 DAO 自动追加过滤条件。
 *
 * <pre>{@code
 * @DataScope(field = "dept_id", type = DataScopeType.DEPT)
 * public List<User> list() { ... }
 * }</pre>
 * <p>
 * 需要业务方实现 {@code DataScopeProvider} 提供范围值，未实现时该接口直接报错。
 *
 * @since 4.1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 过滤字段（列名）
     *
     * @return 列名，可为带表别名的形式
     */
    String field() default "dept_id";

    /**
     * 数据范围类型
     *
     * @return 范围类型
     */
    DataScopeType type() default DataScopeType.DEPT;

}
