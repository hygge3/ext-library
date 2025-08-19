package ext.library.web.validation.validator;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.util.StringUtil;
import ext.library.web.validation.constraints.Cellphone;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 手机号码（中国）校验器
 */
public class CellphoneValidator implements ConstraintValidator<Cellphone, String> {

    boolean notNull;

    @Override
    public void initialize(@Nonnull Cellphone constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isNotBlank(value)) {
            return ValidatorUtil.isMobile(value);
        }

        return !notNull;
    }

}