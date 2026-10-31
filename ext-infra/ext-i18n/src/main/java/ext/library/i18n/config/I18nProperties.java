package ext.library.i18n.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Locale;

/**
 * 国际化配置
 * <p>
 * 前缀 {@code ext.i18n}
 *
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = I18nProperties.PREFIX)
public class I18nProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "ext.i18n";

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 未指定语言时使用的默认语言
     */
    private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;

    /**
     * 资源文件基础路径
     */
    private String resourceBundleBaseName = "i18n/messages";

    /**
     * 翻译表名
     */
    private String tableName = "ext_i18n_message";

    /**
     * 是否启用数据库来源
     * <p>
     * 需要容器中存在 {@code JdbcClient}，未引入时静默关闭该来源。
     */
    private boolean jdbcEnabled = true;

    /**
     * 是否在启动时创建翻译表
     */
    private boolean schemaInit = true;

    /**
     * 缓存有效期
     * <p>
     * 到期后重新加载，便于翻译变更后自动生效；为 0 表示永不过期。
     */
    private Duration cacheTtl = Duration.ofMinutes(5);

    public Duration getCacheTtl() {
        return cacheTtl;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getResourceBundleBaseName() {
        return resourceBundleBaseName;
    }

    public void setResourceBundleBaseName(String resourceBundleBaseName) {
        this.resourceBundleBaseName = resourceBundleBaseName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isJdbcEnabled() {
        return jdbcEnabled;
    }

    public void setJdbcEnabled(boolean jdbcEnabled) {
        this.jdbcEnabled = jdbcEnabled;
    }

    public boolean isSchemaInit() {
        return schemaInit;
    }

    public void setSchemaInit(boolean schemaInit) {
        this.schemaInit = schemaInit;
    }

}
