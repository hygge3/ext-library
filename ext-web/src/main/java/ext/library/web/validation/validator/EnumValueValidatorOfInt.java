package ext.library.web.validation.validator;

import ext.library.web.validation.constraints.OneOfInts;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


/**
 * 枚举值 Validator
 */
public class EnumValueValidatorOfInt implements ConstraintValidator<OneOfInts, Integer> {

    private int[] ints;

    private boolean allowNull;

    @Override
    public void initialize(@Nonnull OneOfInts constraintAnnotation) {
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