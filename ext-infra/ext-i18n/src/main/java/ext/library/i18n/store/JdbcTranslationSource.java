package ext.library.i18n.store;

import ext.library.i18n.TranslationSource;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库翻译源
 * <p>
 * 表结构（可用 {@code ext.i18n.schema-init} 在启动时自动创建）：
 * <pre>{@code
 * CREATE TABLE IF NOT EXISTS ext_i18n_message (
 *     id          BIGSERIAL PRIMARY KEY,
 *     message_key TEXT NOT NULL,
 *     locale      TEXT NOT NULL,
 *     message     TEXT NOT NULL,
 *     UNIQUE (message_key, locale)
 * );
 * }</pre>
 * <p>
 * 查询支持语言降级链：{@code zh-CN} → {@code zh} → {@code ROOT}，
 * 按降级顺序合并，先命中的优先。
 *
 * @since 4.1.0
 */
public class JdbcTranslationSource implements TranslationSource {

    /**
     * 默认表名
     */
    public static final String DEFAULT_TABLE = "ext_i18n_message";

    private final JdbcClient jdbcClient;

    private final String tableName;

    /**
     * 语言 → 翻译表
     */
    private final Map<Locale, Map<String, String>> cache = new ConcurrentHashMap<>();

    public JdbcTranslationSource(JdbcClient jdbcClient, String tableName) {
        this.jdbcClient = jdbcClient;
        this.tableName = tableName;
    }

    @Override
    public Map<String, String> load(Locale locale) {
        return cache.computeIfAbsent(locale, this::doLoad);
    }

    @Override
    public String name() {
        return "jdbc";
    }

    @Override
    public int order() {
        // 数据库源排在文件源之后，覆盖同键翻译
        return 100;
    }

    /**
     * 按降级链查询并合并
     */
    private Map<String, String> doLoad(Locale locale) {
        Map<String, String> messages = new HashMap<>();
        for (String tag : fallbacks(locale)) {
            query(tag).forEach(messages::putIfAbsent);
        }
        return messages;
    }

    /**
     * 查询单个语言的全部翻译
     */
    private Map<String, String> query(String tag) {
        List<MessageRow> rows = jdbcClient.sql("""
                        SELECT message_key, message FROM %s WHERE locale = ?
                        """.formatted(tableName)).param(tag)
                .query((rs, _) -> new MessageRow(rs.getString(1), rs.getString(2))).list();
        Map<String, String> messages = new HashMap<>(rows.size());
        for (MessageRow row : rows) {
            messages.put(row.key(), row.message());
        }
        return messages;
    }

    /**
     * 语言降级链：完整标签 → 仅语言 → 根语言
     */
    static List<String> fallbacks(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String tag = locale.toLanguageTag();
        if (language.isEmpty()) {
            return List.of(Locale.ROOT.toLanguageTag());
        }
        if (country.isEmpty()) {
            return List.of(tag, Locale.ROOT.toLanguageTag());
        }
        return List.of(tag, language, Locale.ROOT.toLanguageTag());
    }

    /**
     * 清除缓存，供翻译变更后热更新
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 创建翻译表，可重复执行
     */
    public void initSchema() {
        jdbcClient.sql("""
                CREATE TABLE IF NOT EXISTS %s (
                    id          BIGSERIAL PRIMARY KEY,
                    message_key TEXT NOT NULL,
                    locale      TEXT NOT NULL,
                    message     TEXT NOT NULL,
                    UNIQUE (message_key, locale)
                )
                """.formatted(tableName)).update();
        Logs.info(EmojiSymbol.I18N, "创建翻译表：table={}", tableName);
    }

    /**
     * 查询结果行
     */
    private record MessageRow(String key, String message) {

    }

}
