package ext.library.web.validation.validator;

import ext.library.tool.util.StringUtil;
import ext.library.tool.util.ValidatorUtil;
import ext.library.web.validation.constraints.Username;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 用户名校验器
 */
public class UsernameValidator implements ConstraintValidator<Username, String> {

    private boolean notNull;

    @Override
    public void initialize(@NonNull Username constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtil.isNotBlank(value)) {
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
