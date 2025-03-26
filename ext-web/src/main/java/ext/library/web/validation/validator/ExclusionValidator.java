package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.BeanUtil;
import ext.library.web.validation.constraints.Exclusion;
import java.util.Map;


/**
 * 互斥关系校验器
 */
public class ExclusionValidator implements ConstraintValidator<Exclusion, Object> {

     String[] exclusions;

    @Override
    public void initialize(@Nonnull Exclusion constraintAnnotation) {
        this.exclusions = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 互斥关系逻辑，多个字段只能其中一个有值
        boolean isExclusionValueExist = false;
        Map<String, Object> objectMap = BeanUtil.beanToMap(value);
        for (String exclusion : exclusions) {
            if (objectMap.containsKey(exclusion)) {
                if (!isExclusionValueExist) {
                    isExclusionValueExist = true;
                } else {
                    // 不满足互斥关系
                    return false;
                }
            }
        }

        return true;
    }

}
