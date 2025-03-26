package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.$;
import ext.library.web.validation.constraints.ZipCode;

/**
 * 邮政编码（中国）校验器
 */
public class ZipCodeValidator implements ConstraintValidator<ZipCode, String> {

     boolean notNull;

    @Override
    public void initialize(@Nonnull ZipCode constraintAnnotation) {
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
