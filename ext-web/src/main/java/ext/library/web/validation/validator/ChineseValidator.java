package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.$;
import ext.library.web.validation.constraints.Chinese;


/**
 * 汉字校验器
 */
public class ChineseValidator implements ConstraintValidator<Chinese, Object> {

    private boolean notNull;

    @Override
    public void initialize(Chinese constraintAnnotation) {
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
