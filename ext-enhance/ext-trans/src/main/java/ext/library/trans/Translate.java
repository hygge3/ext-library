package ext.library.trans;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段翻译注解
 * <p>
 * 标注在需要翻译的字段上，配合 {@link Translator} 实现类完成字段值的自动翻译。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Translate(type = "dict", param = "user_status")
 * private String statusLabel;
 * }</pre>
 *
 * @see Translator
 * @see TranslationType
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = TranslationHandler.class)
public @interface Translate {

    /**
     * 翻译类型
     * <p>
     * 需与 {@link Translator} 实现类上的 {@link TranslationType#value()} 对应
     */
    String type();

    /**
     * 映射字段名
     * <p>
     * 若不为空，则从该字段获取待翻译的值，而非当前字段的值
     */
    String mapper() default "";

    /**
     * 翻译参数
     * <p>
     * 传递给 {@link Translator#translate(Object, String)} 的附加参数，
     * 例如字典类型：{@code param = "sys_user_status"}
     */
    String param() default "";

}
