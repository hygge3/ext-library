package ext.library.i18n.store;

import ext.library.i18n.TranslationSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源文件翻译源
 * <p>
 * 使用 JDK 原生 {@link ResourceBundle}，约定消息文件为 {@code i18n/messages[_zh_CN].properties}，
 * 无需任何第三方依赖。
 * <p>
 * 按语言缓存已解析的结果，改动资源文件需要重启应用才生效。
 *
 * @since 4.1.0
 */
public class ResourceBundleTranslationSource implements TranslationSource {

    /**
     * 资源文件基础路径
     */
    public static final String DEFAULT_BASE_NAME = "i18n/messages";

    /**
     * 语言 → 翻译表
     */
    private final Map<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();

    private final String baseName;

    public ResourceBundleTranslationSource() {
        this(DEFAULT_BASE_NAME);
    }

    public ResourceBundleTranslationSource(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public Map<String, String> load(Locale locale) {
        return cache.computeIfAbsent(locale, this::doLoad);
    }

    @Override
    public String name() {
        return "resource";
    }

    @Override
    public int order() {
        // 文件源最先查询，同键翻译由后面的数据库源覆盖
        return 10;
    }

    /**
     * 读取并解析资源文件
     * <p>
     * 资源文件缺失是正常情况（该语言没翻译），返回空表而不是抛异常。
     */
    private Map<String, String> doLoad(Locale locale) {
        ResourceBundle bundle;
        try {
            bundle = ResourceBundle.getBundle(baseName, locale);
        } catch (MissingResourceException e) {
            return Map.of();
        }
        Map<String, String> messages = new HashMap<>(bundle.keySet().size());
        for (String key : bundle.keySet()) {
            messages.put(key, bundle.getString(key));
        }
        return messages;
    }

    /**
     * 清除缓存，改动资源文件后用于热更新
     */
    @Override
    public void clear() {
        cache.clear();
    }

}
