package ext.library.web.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.web.validation.constraints.OneOfClasses;


/**
 * 枚举值 Validator
 */
public class EnumValueValidatorOfClass implements ConstraintValidator<OneOfClasses, Class<?>> {

    private Class<?>[] classList;

    private boolean allowNull;

    @Override
    public void initialize(OneOfClasses constraintAnnotation) {
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
