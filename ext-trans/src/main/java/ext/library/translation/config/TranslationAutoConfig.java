package ext.library.translation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import ext.library.translation.annotation.TranslationType;
import ext.library.translation.handler.TranslationBeanSerializerModifier;
import ext.library.translation.handler.TranslationHandler;
import ext.library.translation.service.TranslationInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 翻译模块配置类
 */
@AutoConfiguration(after = ObjectMapper.class)
public class TranslationAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final List<TranslationInterface<?>> list;

    private final ObjectMapper objectMapper;

    public TranslationAutoConfig(List<TranslationInterface<?>> list, ObjectMapper objectMapper) {
        this.list = list;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        Map<String, TranslationInterface<?>> map = new HashMap<>(list.size());
        for (TranslationInterface<?> trans : list) {
            if (trans.getClass().isAnnotationPresent(TranslationType.class)) {
                TranslationType annotation = trans.getClass().getAnnotation(TranslationType.class);
                map.put(annotation.type(), trans);
            } else {
                log.warn("[📚] {} 翻译实现类未标注 TranslationType 注解！", trans.getClass().getName());
            }
        }
        TranslationHandler.TRANSLATION_MAPPER.putAll(map);
        // 设置 Bean 序列化修改器
        objectMapper.setSerializerFactory(
                objectMapper.getSerializerFactory().withSerializerModifier(new TranslationBeanSerializerModifier()));
        log.info("[📚] 翻译模块载入成功");

    }

}