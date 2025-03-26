package ext.library.web.validation.validator;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import ext.library.core.util.BeanUtil;
import ext.library.web.validation.constraints.Mutual;
import java.util.Map;


/**
 * 相互关系校验器
 */
public class MutualValidator implements ConstraintValidator<Mutual, Object> {

     String[] mutuals;

    @Override
    public void initialize(@Nonnull Mutual constraintAnnotation) {
        this.mutuals = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 相互关系逻辑，多个字段必须有一个有值
        boolean isMutualValueExist = false;
        for (String mutual : mutuals) {
            Map<String, Object> objectMap = BeanUtil.beanToMap(value);
            if (objectMap.containsKey(mutual)) {
                isMutualValueExist = true;
            }
        }
        // 不满足相互关系
        return isMutualValueExist;
    }

}
