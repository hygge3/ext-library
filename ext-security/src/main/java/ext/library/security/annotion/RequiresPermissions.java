package ext.library.security.annotion;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ext.library.security.enums.Logical;

/**
 * <p>
 * 权限检查
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface RequiresPermissions {

	/**
	 * 权限码
	 */
	String[] value();

	/**
	 * 多个连接条件 AND 所有条件都必须满足 OR 只要满足任意一个条件
	 * @return 条件值
	 */
	Logical logical() default Logical.AND;

}
