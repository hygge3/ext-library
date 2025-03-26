package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.$;
import ext.library.web.validation.constraints.Chinese;


/**
 * 汉字校验器
 */
public class ChineseValidator implements ConstraintValidator<Chinese, Object> {

     boolean notNull;

    @Override
    public void initialize(@Nonnull Chinese constraintAnnotation) {
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
