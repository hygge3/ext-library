package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.web.validation.constraints.Username;
import ext.library.tool.$;
import org.jetbrains.annotations.NotNull;

/**
 * 用户名校验器
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {

	private boolean notNull;

	@Override
	public void initialize(@NotNull Username constraintAnnotation) {
		this.notNull = constraintAnnotation.notNull();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ($.isNotBlank(value)) {
			if (value.length() < 5) {
				return false;
			}
			if (value.contains("@")) {
				return false;
			}

			return !ValidatorUtil.isMobile(value);
		}

		return !notNull;
	}

}
