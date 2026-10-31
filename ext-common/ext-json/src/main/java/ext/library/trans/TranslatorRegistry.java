package ext.library.trans;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 翻译器注册表
 * <p>
 * 管理所有 {@link Translator} 实现类的注册和查找。
 * <p>
 * <b>使用约束：</b>实例状态为 JVM 全局静态，翻译器会持有 DataSource 等资源。
 * 同一 JVM 内若运行多个 Spring 上下文（测试的 {@code @DirtiesContext}、多租户子上下文），
 * 必须先 {@link #clear()} 再重新注册，否则上下文之间会互相覆盖、残留指向已关闭资源的翻译器。
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
        TRANSLATORS.put(Objects.requireNonNull(type, "type is null"),
                Objects.requireNonNull(translator, "translator is null"));
    }

    /**
     * 批量注册翻译器
     * <p>
     * 同类型重复注册时保留<b>先注册</b>的实例并输出告警，避免静默覆盖。
     * 装配阶段（{@link TranslationAutoConfiguration}）已做重复检测并直接启动失败，
     * 这里只是防御跨上下文重新注册的场景。
     *
     * @param translators 翻译器映射
     */
    public static void registerAll(Map<String, Translator<?>> translators) {
        translators.forEach((type, translator) -> {
            Translator<?> old = TRANSLATORS.putIfAbsent(type, translator);
            if (old != null && old != translator) {
                Logs.warn(EmojiSymbol.TRANS, "翻译类型 {} 重复注册：{} 覆盖了已注册的 {}",
                        type, translator.getClass().getName(), old.getClass().getName());
            }
        });
    }

    /**
     * 获取翻译器
     *
     * @param type 翻译类型
     *
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
     * 清空所有翻译器
     * <p>
     * Spring 上下文关闭时必须调用，否则翻译器持有的资源不会释放。
     */
    public static void clear() {
        TRANSLATORS.clear();
    }

}
