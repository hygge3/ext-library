package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.web.validation.constraints.Chinese;
import ext.library.tool.$;
import org.jetbrains.annotations.NotNull;


/**
 * 汉字校验器
 */
public class ChineseValidator implements ConstraintValidator<Chinese, Object> {

	private boolean notNull;

	@Override
	public void initialize(@NotNull Chinese constraintAnnotation) {
		this.notNull = constraintAnnotation.notNull();
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		String validValue = String.valueOf(value);
		if ($.isNotBlank(validValue)) {
			return ValidatorUtil.isChinese(validValue);
		}

		return !notNull;
	}

}
