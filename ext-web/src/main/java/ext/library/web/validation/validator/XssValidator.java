package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.web.validation.constraints.Xss;

/**
 * 自定义 xss 校验注解实现
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

     boolean notNull;

    @Override
    public void initialize(@Nonnull Xss constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return ValidatorUtil.hasXss(value);
    }

}
