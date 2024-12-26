package ext.library.translation.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.library.translation.annotation.TranslationType;
import ext.library.translation.handler.TranslationBeanSerializerModifier;
import ext.library.translation.handler.TranslationHandler;
import ext.library.translation.service.TranslationInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * 翻译模块配置类
 */
@Slf4j
@RequiredArgsConstructor
@AutoConfiguration(after = ObjectMapper.class)
public class TranslationAutoConfig {

	private final List<TranslationInterface<?>> list;

	private final ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		Map<String, TranslationInterface<?>> map = new HashMap<>(list.size());
		for (TranslationInterface<?> trans : list) {
			if (trans.getClass().isAnnotationPresent(TranslationType.class)) {
				TranslationType annotation = trans.getClass().getAnnotation(TranslationType.class);
				map.put(annotation.type(), trans);
			}
			else {
				log.warn("{} 翻译实现类未标注 TranslationType 注解！", trans.getClass().getName());
			}
		}
		TranslationHandler.TRANSLATION_MAPPER.putAll(map);
		// 设置 Bean 序列化修改器
		objectMapper.setSerializerFactory(
				objectMapper.getSerializerFactory().withSerializerModifier(new TranslationBeanSerializerModifier()));
	}

}
