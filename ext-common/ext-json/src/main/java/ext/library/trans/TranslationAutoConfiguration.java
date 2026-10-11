package ext.library.trans;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import tools.jackson.databind.json.JsonMapper;

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

    private final List<Translator<?>> translators;

    public TranslationAutoConfiguration(List<Translator<?>> translators) {
        this.translators = translators;
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
                Logs.warn(EmojiSymbol.TRANS, "{} 未标注 @TranslationType 注解", clazz.getName());
            }
        }
        TranslatorRegistry.registerAll(translatorMap);
        Logs.info(EmojiSymbol.TRANS, "载入模块：翻译，已注册 {} 个翻译器", translatorMap.size());
    }

}
