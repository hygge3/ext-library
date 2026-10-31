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
 * 标注在 String 类型字段（或其 getter）上，JSON 序列化时自动进行脱敏处理
 *
 * <pre>{@code
 * public class User {
 *     // 只写 @Sensitive 即走默认的 GENERIC 策略（前 1/3 + 后 1/3）
 *     private String remark;
 *
 *     @Sensitive(strategy = DesensitizeStrategy.PHONE)
 *     private String phone;
 *
 *     @Sensitive(strategy = DesensitizeStrategy.ID_CARD)
 *     private String idCard;
 *
 *     // 显式跳过脱敏（不推荐，除非确认字段确实不敏感）
 *     @Sensitive(strategy = DesensitizeStrategy.NONE)
 *     private String nickname;
 *
 *     // 使用自定义规则
 *     @Sensitive(customRule = MyCustomRule.class)
 *     private String customField;
 * }
 * }</pre>
 * <p>
 * <b>类型限制：</b>仅对 {@link String} 字段生效。字段声明为 {@code Object} / {@code CharSequence}
 * 等其他类型时注解不会生效（启动/首次序列化时会有告警日志），字段将输出明文。
 * <p>
 * <b>禁止回写：</b>本注解只作用于序列化方向，但脱敏后的值（如 {@code 138****5678}）不是合法手机号。
 * 严禁把脱敏后的序列化结果用于持久化、缓存、ES 或消息队列，否则会写入不可用的脏数据。
 * <p>
 * 长度不守恒的策略：{@code PASSWORD} 恒为 6 字符 {@code ******}；{@code EMAIL} 域名完整保留故长度不定；
 * 其余策略均等长。需要长度守恒时优先用等长策略。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {

    /**
     * 脱敏策略，默认 {@link DesensitizeStrategy#GENERIC}（保留前 1/3 + 后 1/3）
     * <p>
     * 默认走通用脱敏而非原样输出：只写 {@code @Sensitive} 就应得到脱敏结果，
     * 漏写策略导致的明文输出比脱敏过重严重得多。
     *
     * @return 脱敏策略枚举
     */
    DesensitizeStrategy strategy() default DesensitizeStrategy.GENERIC;

    /**
     * 自定义脱敏规则
     * <p>
     * 当指定自定义规则时，{@link #strategy()} 将被忽略
     *
     * @return 自定义规则实现类
     */
    Class<? extends DesensitizeRule> customRule() default NoOpDesensitizeRule.class;
}
