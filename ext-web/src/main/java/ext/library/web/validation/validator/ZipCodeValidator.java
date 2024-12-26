package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.web.validation.constraints.ZipCode;
import ext.library.tool.$;
import org.jetbrains.annotations.NotNull;

/**
 * 邮政编码（中国）校验器
 */
public class ZipCodeValidator implements ConstraintValidator<ZipCode, String> {

	private boolean notNull;

	@Override
	public void initialize(@NotNull ZipCode constraintAnnotation) {
		this.notNull = constraintAnnotation.notNull();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if ($.isNotBlank(value)) {
			return ValidatorUtil.isZipCode(value);
		}

		return !notNull;
	}

}
