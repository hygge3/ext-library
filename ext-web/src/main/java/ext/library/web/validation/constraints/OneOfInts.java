package ext.library.web.validation.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import ext.library.web.validation.validator.EnumValueValidatorOfInt;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 整数之一
 */
@Target({ METHOD, FIELD, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(OneOfInts.List.class)
@Documented
@Constraint(validatedBy = { EnumValueValidatorOfInt.class })
public @interface OneOfInts {

	String message() default "{validation.annotation.OneOfInts.message}";

	int[] value();

	/**
	 * 允许值为 null, 默认不允许
	 */
	boolean allowNull() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	@interface List {

		OneOfInts[] value();

	}

}
