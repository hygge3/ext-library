package ext.library.web.validation.validator;

import ext.library.core.util.BeanUtil;
import ext.library.web.validation.constraints.Mutual;
import org.jspecify.annotations.NonNull;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Map;


/**
 * 相互关系校验器
 */
public class MutualValidator implements ConstraintValidator<Mutual, Object> {

    private String[] mutuals;

    @Override
    public void initialize(@NonNull Mutual constraintAnnotation) {
        this.mutuals = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 相互关系逻辑，多个字段必须有一个有值
        Map<String, Object> objectMap = BeanUtil.beanToMap(value);
        for (String mutual : mutuals) {
            // 判的是「有值」不是「字段存在」：beanToMap 对每个属性无条件 put，containsKey 恒为真
            if (objectMap.get(mutual) != null) {
                return true;
            }
        }
        // 不满足相互关系
        return false;
    }

}