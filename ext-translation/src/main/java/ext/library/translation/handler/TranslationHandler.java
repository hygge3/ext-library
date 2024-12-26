package ext.library.translation.handler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import ext.library.translation.annotation.Translation;
import ext.library.translation.service.TranslationInterface;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.StringUtils;

/**
 * 翻译处理器
 */
@Slf4j
public class TranslationHandler extends JsonSerializer<Object> implements ContextualSerializer {

	/**
	 * 全局翻译实现类映射器
	 */
	public static final Map<String, TranslationInterface<?>> TRANSLATION_MAPPER = new ConcurrentHashMap<>();

	private Translation translation;

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		TranslationInterface<?> trans = TRANSLATION_MAPPER.get(translation.type());
		if (Objects.nonNull(trans)) {
			// 如果映射字段不为空 则取映射字段的值
			if (StringUtils.hasText(translation.mapper())) {
				BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(gen.currentValue());
				value = beanWrapper.getPropertyValue(translation.mapper());
			}
			// 如果为 null 直接写出
			if (Objects.isNull(value)) {
				gen.writeNull();
				return;
			}
			Object result = trans.translation(value, translation.other());
			gen.writeObject(result);
		}
		else {
			gen.writeObject(value);
		}
	}

	@Override
	public JsonSerializer<?> createContextual(SerializerProvider prov,@NotNull BeanProperty property)
			throws JsonMappingException {
		Translation translation = property.getAnnotation(Translation.class);
		if (Objects.nonNull(translation)) {
			this.translation = translation;
			return this;
		}
		return prov.findValueSerializer(property.getType(), property);
	}

}
