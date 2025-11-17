package ext.library.web.validation.validator;

import ext.library.core.util.ValidatorUtil;
import ext.library.web.validation.constraints.Xss;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 自定义 xss 校验注解实现
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

    private boolean notNull;

    @Override
    public void initialize(@NonNull Xss constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return ValidatorUtil.hasXss(value);
    }

}