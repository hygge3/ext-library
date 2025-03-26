package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.$;
import ext.library.web.validation.constraints.Username;

/**
 * 用户名校验器
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {

     boolean notNull;

    @Override
    public void initialize(@Nonnull Username constraintAnnotation) {
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
