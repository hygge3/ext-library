package ext.library.trans;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * 翻译处理器
 * <p>
 * Jackson 序列化器，用于在序列化时自动翻译标注了 {@link Translate} 注解的字段。
 */
public class TranslationHandler extends JsonSerializer<Object> implements ContextualSerializer {

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
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
            gen.writeObject(result);
        } else {
            gen.writeObject(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        Translate translate = property.getAnnotation(Translate.class);
        if (translate != null) {
            this.annotation = translate;
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }

}
