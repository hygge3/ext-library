package ext.library.web.validation.validator;

import ext.library.tool.util.CollectionUtil;
import ext.library.web.validation.constraints.RangeIn;
import org.springframework.util.StringUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 范围验证
 *
 * @author dy
 */
public class RangeInValidator implements ConstraintValidator<RangeIn, Object> {

    private RangeIn rangeIn;

    @Override
    public void initialize(RangeIn constraintAnnotation) {
        this.rangeIn = constraintAnnotation;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // 如果 value 为空则不进行验证，为空验证可以使用 @NotBlank @NotNull @NotEmpty 等注解来进行控制，职责分离
        if (value == null) {
            return true;
        }
        String[] ranges = StringUtils.delimitedListToStringArray(rangeIn.value(), ",", " \t\n\n\f");
        switch (value) {
            case CharSequence obj -> {
                return CollectionUtil.contains(ranges, obj);
            }
            case Number obj -> {
                return CollectionUtil.contains(ranges, obj.toString());
            }
            case Collection obj -> {
                return obj.stream().allMatch(it -> CollectionUtil.contains(ranges, it.toString()));
            }
            case Iterable obj -> {
                AtomicBoolean flag = new AtomicBoolean(true);
                obj.forEach(it -> {
                    if (!CollectionUtil.contains(ranges, it.toString())) {
                        flag.set(false);
                    }
                });
                return flag.get();
            }
            case Object[] obj -> {
                return Arrays.stream(obj).allMatch(it -> CollectionUtil.contains(ranges, it.toString()));
            }
            default -> {
                return false;
            }
        }
    }

}