package ext.library.i18n;

import ext.library.i18n.store.ResourceBundleTranslationSource;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 翻译管理器
 * <p>
 * 串联多个 {@link TranslationSource}，按 {@link TranslationSource#order()} 从小到大依次查询，
 * 先命中的翻译优先；全部未命中时返回消息键本身，便于暴露缺失而不返回 null。
 * <p>
 * 翻译内容按语言缓存，需要变更时调用 {@link #clear()}。
 *
 * @since 4.1.0
 */
public class I18nMessageManager {

    private final List<TranslationSource> sources;

    /**
     * 默认语言
     */
    private final Locale defaultLocale;

    /**
     * 语言 → 合并后的翻译表
     */
    private final ConcurrentMap<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();

    public I18nMessageManager(List<TranslationSource> sources, Locale defaultLocale) {
        this.sources = sources.stream().sorted((a, b) -> Integer.compare(a.order(), b.order())).toList();
        this.defaultLocale = defaultLocale;
        this.sources.forEach(source -> Logs.info(EmojiSymbol.I18N, "载入翻译来源：name={}, order={}",
                source.name(), source.order()));
    }

    /**
     * 缺省语言
     *
     * @return 默认语言
     */
    public Locale defaultLocale() {
        return defaultLocale;
    }

    /**
     * 按缺省语言取翻译
     *
     * @param messageKey 消息键
     *
     * @return 翻译文本，未命中时返回消息键本身
     */
    public String get(String messageKey) {
        return get(messageKey, defaultLocale);
    }

    /**
     * 按指定语言取翻译
     *
     * @param messageKey 消息键
     * @param locale     语言
     *
     * @return 翻译文本，未命中时返回消息键本身
     */
    public String get(String messageKey, Locale locale) {
        return find(messageKey, locale).orElse(messageKey);
    }

    /**
     * 按指定语言取翻译
     *
     * @param messageKey 消息键
     * @param locale     语言
     *
     * @return 翻译文本，未命中时为空
     */
    public Optional<String> find(String messageKey, Locale locale) {
        if (messageKey == null || messageKey.isBlank()) {
            return Optional.empty();
        }
        String message = messages(locale).get(messageKey);
        return message == null ? Optional.empty() : Optional.of(message);
    }

    /**
     * 按指定语言取翻译并替换占位符
     * <p>
     * 占位符采用 {@link java.text.MessageFormat} 语法，如 {@code {0}}、{@code {1,number,#.##}}。
     * 消息键未命中时返回键本身，此时占位符不会被替换。
     *
     * @param messageKey 消息键
     * @param locale     语言
     * @param args       插值参数
     *
     * @return 翻译文本
     */
    public String get(String messageKey, Locale locale, Object... args) {
        String message = get(messageKey, locale);
        if (args.length == 0) {
            return message;
        }
        return new MessageFormat(message, locale).format(args);
    }

    /**
     * 指定语言是否已翻译
     *
     * @param messageKey 消息键
     * @param locale     语言
     *
     * @return 已翻译时为 true
     */
    public boolean contains(String messageKey, Locale locale) {
        return find(messageKey, locale).isPresent();
    }

    /**
     * 取合并后的翻译表
     */
    private Map<String, String> messages(Locale locale) {
        return cache.computeIfAbsent(locale, key -> {
            Map<String, String> merged = new LinkedHashMap<>();
            for (TranslationSource source : sources) {
                Map<String, String> loaded = source.load(key);
                if (loaded != null && !loaded.isEmpty()) {
                    loaded.forEach(merged::putIfAbsent);
                }
            }
            return Map.copyOf(merged);
        });
    }

    /**
     * 清除缓存，翻译内容变更后调用
     */
    public void clear() {
        cache.clear();
        for (TranslationSource source : sources) {
            if (source instanceof ResourceBundleTranslationSource bundle) {
                bundle.clear();
            }
        }
    }

}
