package ext.library.trans;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 翻译类型注解
 * <p>
 * 标注在 {@link Translator} 实现类上，声明该实现支持的翻译类型。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Component
 * @TranslationType("dict")
 * public class DictTranslator implements Translator<String> {
 *     @Override
 *     public String translate(Object key, String param) {
 *         return dictService.getLabel(param, key.toString());
 *     }
 * }
 * }</pre>
 *
 * @see Translator
 * @see Translate
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface TranslationType {

    /**
     * 翻译类型标识
     * <p>
     * 与 {@link Translate#type()} 对应
     */
    String value();

}
