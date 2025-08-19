package ext.library.web.validation.validator;

import ext.library.core.util.ValidatorUtil;
import ext.library.tool.util.StringUtil;
import ext.library.web.validation.constraints.English;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


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
        if (StringUtil.isNotBlank(value)) {
            return ValidatorUtil.isWord(value);
        }

        return !notNull;
    }

}