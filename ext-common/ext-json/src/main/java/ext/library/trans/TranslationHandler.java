package ext.library.trans;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Map;

/**
 * 翻译处理器
 * <p>
 * Jackson 序列化器，用于在序列化时自动翻译标注了 {@link Translate} 注解的字段。
 */
public class TranslationHandler extends ValueSerializer<Object> {

    private final Map<String, Translator<?>> translators;
    private Translate annotation;

    /**
     * 默认构造器（Jackson 反射创建时使用）
     */
    public TranslationHandler() {
        this.translators = TranslatorRegistry.getTranslators();
    }

    /**
     * 带翻译器映射的构造器
     *
     * @param translators 翻译器映射
     */
    public TranslationHandler(Map<String, Translator<?>> translators) {
        this.translators = translators;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        Translator<?> translator = translators.get(annotation.type());
        if (translator != null) {
            // 如果指定了映射字段，则从映射字段获取值
            if (StringUtils.hasText(annotation.mapper())) {
                BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(gen.currentValue());
                value = beanWrapper.getPropertyValue(annotation.mapper());
            }
            // 空值直接写出
            if (value == null) {
                gen.writeNull();
                return;
            }
            Object result = translator.translate(value, annotation.param());
            gen.writePOJO(result);
        } else {
            gen.writePOJO(value);
        }
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }
        Translate translate = property.getAnnotation(Translate.class);
        if (translate != null) {
            this.annotation = translate;
            return this;
        }
        return this;
    }

}
