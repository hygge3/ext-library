package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.web.validation.constraints.OneOfStrings;
import org.jetbrains.annotations.NotNull;


/**
 * 枚举值 Validator
 */
public class EnumValueValidatorOfString implements ConstraintValidator<OneOfStrings, String> {

	private String[] stringList;

	private boolean allowNull;

	@Override
	public void initialize(@NotNull OneOfStrings constraintAnnotation) {
		this.stringList = constraintAnnotation.value();
		this.allowNull = constraintAnnotation.allowNull();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null) {
			return this.allowNull;
		}
		for (String strValue : this.stringList) {
			if (strValue.equals(value)) {
				return true;
			}
		}
		return false;
	}

}
