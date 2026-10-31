package ext.library.web.validation.validator;

import ext.library.core.util.BeanUtil;
import ext.library.web.validation.constraints.Exclusion;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;


/**
 * 互斥关系校验器
 */
public class ExclusionValidator implements ConstraintValidator<Exclusion, Object> {

    private String[] exclusions;

    @Override
    public void initialize(@NonNull Exclusion constraintAnnotation) {
        this.exclusions = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 互斥关系逻辑，多个字段只能其中一个有值
        Map<String, Object> objectMap = BeanUtil.beanToMap(value);
        boolean isExclusionValueExist = false;
        for (String exclusion : exclusions) {
            // 判的是「有值」不是「字段存在」：beanToMap 对每个属性无条件 put，containsKey 恒为真
            if (objectMap.get(exclusion) != null) {
                if (isExclusionValueExist) {
                    // 不满足互斥关系
                    return false;
                }
                isExclusionValueExist = true;
            }
        }

        return true;
    }

}