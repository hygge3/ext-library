package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.$;
import ext.library.web.validation.constraints.Cellphone;

/**
 * 手机号码（中国）校验器
 */
public class CellphoneValidator implements ConstraintValidator<Cellphone, String> {

    private boolean notNull;

    @Override
    public void initialize(Cellphone constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if ($.isNotBlank(value)) {
            return ValidatorUtil.isMobile(value);
        }

        return !notNull;
    }

}
