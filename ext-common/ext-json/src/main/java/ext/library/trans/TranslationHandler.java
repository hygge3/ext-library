package ext.library.trans;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 翻译处理器
 * <p>
 * Jackson 序列化器，用于在序列化时自动翻译标注了 {@link Translate} 注解的字段。
 * <p>
 * 实例状态全部为不可变字段：{@link #createContextual} 针对每个属性返回一个新实例，
 * 避免多字段共享同一 serializer 时注解互相覆盖。
 */
public class TranslationHandler extends ValueSerializer<Object> {

    /** 翻译结果缓存的 key，挂在 {@link SerializationContext} 上，随序列化结束自动回收 */
    private static final Object CACHE_KEY = TranslationCache.class;

    /** 缓存 null 结果的哨兵（{@link ConcurrentHashMap} 不允许 null value） */
    private static final Object NULL_SENTINEL = new Object();

    /** 翻译最大嵌套深度，超过后退化为原样输出，防止翻译结果里再含 {@link Translate} 字段导致栈溢出 */
    private static final int MAX_DEPTH = 3;

    /** 已告警过的翻译类型，避免序列化热路径上每次都打日志冲垮日志文件 */
    private static final Set<String> WARNED_TYPES = ConcurrentHashMap.newKeySet();

    /** 翻译类型，空表示不做翻译 */
    private final String type;

    /** 翻译参数 */
    private final String param;

    /**
     * 单次序列化内的翻译状态：结果缓存 + 当前嵌套深度
     * <p>
     * 挂在 {@link SerializationContext} 上，序列化结束即回收，不存在跨请求污染或线程池泄漏。
     */
    private static final class TranslationCache {

        private final Map<CacheKey, Object> results = new ConcurrentHashMap<>();

        private int depth;
    }

    /**
     * 默认构造器（Jackson 反射创建时使用）
     * <p>
     * 未做上下文解析，此时不做翻译，原样写出。
     */
    public TranslationHandler() {
        this.type = "";
        this.param = "";
    }

    private TranslationHandler(String type, String param) {
        this.type = type;
        this.param = param;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (type.isEmpty()) {
            // 未解析到 @Translate 注解，原样输出
            gen.writePOJO(value);
            return;
        }
        Translator<?> translator = TranslatorRegistry.get(type);
        if (translator == null) {
            // 稳态配置错误，每个类型只告警一次，否则会刷爆日志
            if (WARNED_TYPES.add(type)) {
                Logs.warn(EmojiSymbol.TRANS, "未找到翻译器：{}，字段将原样输出 code", type);
            }
            gen.writePOJO(value);
            return;
        }

        if (value == null) {
            gen.writeNull();
            return;
        }

        // 翻译结果里可能又含 @Translate 字段，限制深度避免栈溢出
        TranslationCache cache = cacheOf(ctxt);
        if (cache.depth >= MAX_DEPTH) {
            Logs.warn(EmojiSymbol.TRANS, "翻译嵌套超过 {} 层，type={} 字段原样输出", MAX_DEPTH, type);
            gen.writePOJO(value);
            return;
        }

        Object translated = translateCached(translator, value, cache);
        if (translated == null) {
            // Translator 允许返回 null，不能直接 writePOJO(null)
            gen.writeNull();
            return;
        }
        cache.depth++;
        try {
            gen.writePOJO(translated);
        } finally {
            cache.depth--;
        }
    }

    /**
     * 翻译并复用本次序列化内相同键的结果
     * <p>
     * 避免序列化 1000 条记录时对同一批 id 发起 1000 次翻译调用。
     */
    private Object translateCached(Translator<?> translator, Object value, TranslationCache cache) {
        CacheKey key = new CacheKey(translator, value, param);
        Object cached = cache.results.computeIfAbsent(key, k -> {
            Object translated = translator.translate(value, param);
            return translated != null ? translated : NULL_SENTINEL;
        });
        return cached == NULL_SENTINEL ? null : cached;
    }

    private static TranslationCache cacheOf(SerializationContext ctxt) {
        // Jackson 3 的 getAttribute 返回原始 Object，需要自己强转
        TranslationCache cache = (TranslationCache) ctxt.getAttribute(CACHE_KEY);
        if (cache == null) {
            cache = new TranslationCache();
            ctxt.setAttribute(CACHE_KEY, cache);
        }
        return cache;
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }
        Translate translate = property.getAnnotation(Translate.class);
        if (translate == null) {
            return this;
        }
        // 每个属性返回独立实例，字段不可变
        return new TranslationHandler(translate.type(), translate.param());
    }

    /**
     * 翻译缓存键：同一翻译器 + 同一入参 + 同一 param 的结果可复用
     *
     * @param translator 翻译器实例
     * @param value      待翻译的值
     * @param param      翻译参数
     */
    private record CacheKey(Translator<?> translator, Object value, String param) {
        private CacheKey {
            Objects.requireNonNull(translator, "translator is null");
        }
    }

}
