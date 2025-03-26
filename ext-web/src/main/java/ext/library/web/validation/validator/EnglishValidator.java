package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.$;
import ext.library.web.validation.constraints.English;


/**
 * 字母（包括大写和小写字母）校验器
 */
public class EnglishValidator implements ConstraintValidator<English, String> {

     boolean notNull;

    @Override
    public void initialize(@Nonnull English constraintAnnotation) {
        this.notNull = constraintAnnotation.notNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if ($.isNotBlank(value)) {
            return ValidatorUtil.isWord(value);
        }

        return !notNull;
    }

}
