package ext.library.web.validation.validator;

import ext.library.web.validation.constraints.OneOfClasses;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * OneOfClasses 约束验证器
 */
public class OneOfClassesValidator implements ConstraintValidator<OneOfClasses, Class<?>> {

    private Class<?>[] classList;

    private boolean allowNull;

    @Override
    public void initialize(@NonNull OneOfClasses constraintAnnotation) {
        this.classList = constraintAnnotation.value();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Class<?> value, ConstraintValidatorContext context) {
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
