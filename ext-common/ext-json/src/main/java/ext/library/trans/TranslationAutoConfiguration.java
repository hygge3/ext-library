package ext.library.trans;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.annotation.AnnotationUtils;
import tools.jackson.databind.json.JsonMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 翻译模块自动配置
 */
@AutoConfiguration
@ConditionalOnClass(JsonMapper.class)
@EnableConfigurationProperties(TranslationProperties.class)
@ConditionalOnProperty(prefix = TranslationProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class TranslationAutoConfiguration {

    private final List<Translator<?>> translators;

    public TranslationAutoConfiguration(List<Translator<?>> translators) {
        this.translators = translators;
    }

    /**
     * 装配翻译器
     * <p>
     * 1. 必须穿透 Spring AOP 代理取目标类，否则带 {@code @Transactional} / {@code @Async} / {@code @Cacheable}
     * 的翻译器会因 {@code getClass()} 返回 CGLIB 子类而丢失 {@link TranslationType} 注解，
     * 表现为该字段静默输出原始 code。
     * 2. 同一 type 重复注册属于配置错误，直接启动失败——静默覆盖会让「谁生效」取决于 bean 定义顺序。
     */
    @PostConstruct
    public void init() {
        Map<String, Translator<?>> translatorMap = new HashMap<>(translators.size());
        for (Translator<?> translator : translators) {
            Class<?> clazz = AopUtils.getTargetClass(translator);
            TranslationType annotation = AnnotationUtils.findAnnotation(clazz, TranslationType.class);
            if (annotation == null) {
                Logs.warn(EmojiSymbol.TRANS, "{} 未标注 @TranslationType 注解", clazz.getName());
                continue;
            }
            Translator<?> previous = translatorMap.putIfAbsent(annotation.value(), translator);
            if (previous != null) {
                throw new IllegalStateException("翻译类型重复注册: '" + annotation.value() + "' -> "
                        + AopUtils.getTargetClass(previous).getName() + " / " + clazz.getName());
            }
        }
        TranslatorRegistry.registerAll(translatorMap);
        Logs.info(EmojiSymbol.TRANS, "载入模块：翻译，已注册 {} 个翻译器", translatorMap.size());
    }

    /**
     * 上下文关闭时清空全局注册表，避免翻译器持有已关闭的 DataSource
     */
    @PreDestroy
    public void destroy() {
        TranslatorRegistry.clear();
    }

}
