package ext.library.web.validation.validator;

import ext.library.tool.util.StringUtil;
import ext.library.tool.util.ValidatorUtil;
import ext.library.web.validation.constraints.Cellphone;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 手机号码（中国）校验器
 */
public class CellphoneValidator implements ConstraintValidator<Cellphone, String> {

    private boolean notNull;

    @Override
    public void initialize(@NonNull Cellphone constraintAnnotation) {
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
