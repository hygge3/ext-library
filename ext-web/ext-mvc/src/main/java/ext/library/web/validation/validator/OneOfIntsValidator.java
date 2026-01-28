package ext.library.web.validation.validator;

import ext.library.web.validation.constraints.OneOfInts;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * OneOfInts 约束验证器
 */
public class OneOfIntsValidator implements ConstraintValidator<OneOfInts, Integer> {

    private int[] ints;

    private boolean allowNull;

    @Override
    public void initialize(@NonNull OneOfInts constraintAnnotation) {
        this.ints = constraintAnnotation.value();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return this.allowNull;
        }
        for (int anInt : this.ints) {
            if (anInt == value) {
                return true;
            }
        }
        return false;
    }

}
