package ext.library.web.validation.validator;

import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.BeanUtil;
import ext.library.web.validation.constraints.Exclusion;


/**
 * 互斥关系校验器
 */
public class ExclusionValidator implements ConstraintValidator<Exclusion, Object> {

    private String[] exclusions;

    @Override
    public void initialize(Exclusion constraintAnnotation) {
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
