package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.web.validation.constraints.OneOfStrings;


/**
 * 枚举值 Validator
 */
public class EnumValueValidatorOfString implements ConstraintValidator<OneOfStrings, String> {

     String[] stringList;

     boolean allowNull;

    @Override
    public void initialize(@Nonnull OneOfStrings constraintAnnotation) {
        this.stringList = constraintAnnotation.value();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return this.allowNull;
        }
        for (String strValue : this.stringList) {
            if (strValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
