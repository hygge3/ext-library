package ext.library.trans;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 翻译模块自动配置
 */
@AutoConfiguration
@ConditionalOnClass(JsonMapper.class)
public class TranslationAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TranslationAutoConfiguration.class);

    private final List<Translator<?>> translators;
    private final JsonMapper jsonMapper;

    public TranslationAutoConfiguration(List<Translator<?>> translators, JsonMapper jsonMapper) {
        this.translators = translators;
        this.jsonMapper = jsonMapper;
    }

    @PostConstruct
    public void init() {
        Map<String, Translator<?>> translatorMap = new HashMap<>(translators.size());
        for (Translator<?> translator : translators) {
            Class<?> clazz = translator.getClass();
            if (clazz.isAnnotationPresent(TranslationType.class)) {
                TranslationType annotation = clazz.getAnnotation(TranslationType.class);
                translatorMap.put(annotation.value(), translator);
            } else {
                log.warn("[Translation] {} 未标注 @TranslationType 注解", clazz.getName());
            }
        }
        TranslatorRegistry.registerAll(translatorMap);
        // 设置 Bean 序列化修改器
        jsonMapper.setSerializerFactory(
                jsonMapper.getSerializerFactory()
                        .withSerializerModifier(new TranslationSerializerModifier())
        );
        log.info("[Translation] 翻译模块初始化完成，已注册 {} 个翻译器", translatorMap.size());
    }

}
