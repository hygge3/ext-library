package ext.library.web.validation.validator;

import ext.library.web.validation.constraints.OneOfClasses;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * 枚举值 Validator
 */
public class EnumValueValidatorOfClass implements ConstraintValidator<OneOfClasses, Class<?>> {

    private Class<?>[] classList;

    private boolean allowNull;

    @Override
    public void initialize(@Nonnull OneOfClasses constraintAnnotation) {
        this.classList = constraintAnnotation.value();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Class value, ConstraintValidatorContext context) {
        if (value == null) {
            return this.allowNull;
        }
        for (Class<?> clazz : this.classList) {
            if (clazz.isAssignableFrom(value)) {
                return true;
            }
        }
        return false;
    }

}