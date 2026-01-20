package ext.library.trans;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 翻译器注册表
 * <p>
 * 管理所有 {@link Translator} 实现类的注册和查找。
 */
public final class TranslatorRegistry {

    private static final Map<String, Translator<?>> TRANSLATORS = new ConcurrentHashMap<>();

    private TranslatorRegistry() {
        // 防止实例化
    }

    /**
     * 注册翻译器
     *
     * @param type       翻译类型
     * @param translator 翻译器实例
     */
    public static void register(String type, Translator<?> translator) {
        TRANSLATORS.put(type, translator);
    }

    /**
     * 批量注册翻译器
     *
     * @param translators 翻译器映射
     */
    public static void registerAll(Map<String, Translator<?>> translators) {
        TRANSLATORS.putAll(translators);
    }

    /**
     * 获取翻译器
     *
     * @param type 翻译类型
     * @return 翻译器实例，不存在则返回 null
     */
    public static Translator<?> get(String type) {
        return TRANSLATORS.get(type);
    }

    /**
     * 获取所有翻译器（只读视图）
     *
     * @return 翻译器映射的不可变视图
     */
    public static Map<String, Translator<?>> getTranslators() {
        return Collections.unmodifiableMap(TRANSLATORS);
    }

    /**
     * 清空所有翻译器（仅用于测试）
     */
    static void clear() {
        TRANSLATORS.clear();
    }

}
