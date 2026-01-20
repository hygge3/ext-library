package ext.library.desensitize;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 敏感数据脱敏注解
 * <p>
 * 标注在 String 类型字段上，JSON 序列化时自动进行脱敏处理
 *
 * <pre>{@code
 * public class User {
 *     @Sensitive(strategy = DesensitizeStrategy.PHONE)
 *     private String phone;
 *
 *     @Sensitive(strategy = DesensitizeStrategy.ID_CARD)
 *     private String idCard;
 *
 *     // 使用自定义规则
 *     @Sensitive(customRule = MyCustomRule.class)
 *     private String customField;
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {

    /**
     * 脱敏策略
     *
     * @return 脱敏策略枚举
     */
    DesensitizeStrategy strategy() default DesensitizeStrategy.NONE;

    /**
     * 自定义脱敏规则
     * <p>
     * 当指定自定义规则时，{@link #strategy()} 将被忽略
     *
     * @return 自定义规则实现类
     */
    Class<? extends DesensitizeRule> customRule() default NoOpDesensitizeRule.class;
}
