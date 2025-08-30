package ext.library.web.validation.validator;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.util.StringUtil;
import ext.library.web.validation.constraints.ZipCode;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 邮政编码（中国）校验器
 */
public class ZipCodeValidator implements ConstraintValidator<ZipCode, String> {

    private boolean notNull;

    @Override
    public void initialize(@Nonnull ZipCode constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isNotBlank(value)) {
            return ValidatorUtil.isZipCode(value);
        }

        return !notNull;
    }

}