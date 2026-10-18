package ext.library.i18n.config;

import ext.library.i18n.I18nMessageManager;
import ext.library.i18n.TranslationSource;
import ext.library.i18n.store.JdbcTranslationSource;
import ext.library.i18n.store.ResourceBundleTranslationSource;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

/**
 * 国际化自动配置
 * <p>
 * 默认装载资源文件来源；容器中存在 {@link JdbcClient} 且未显式关闭时叠加数据库来源。
 * 数据库来源排在文件来源之后，仅覆盖同名消息键，不删除文件中的翻译。
 * 业务方可注册自己的 {@link TranslationSource} 接入远程配置中心。
 *
 * @since 4.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(I18nProperties.class)
@ConditionalOnProperty(prefix = I18nProperties.PREFIX, name = "enabled", havingValue = "true",
        matchIfMissing = true)
@Import(I18nAutoConfiguration.JdbcSourceConfiguration.class)
public class I18nAutoConfiguration {

    /**
     * 资源文件翻译源
     *
     * @param properties 配置
     *
     * @return 翻译来源
     */
    @Bean
    @ConditionalOnMissingBean(ResourceBundleTranslationSource.class)
    public ResourceBundleTranslationSource resourceBundleTranslationSource(I18nProperties properties) {
        return new ResourceBundleTranslationSource(properties.getResourceBundleBaseName());
    }

    /**
     * 翻译管理器
     *
     * @param properties 配置
     * @param sources    翻译来源
     *
     * @return 翻译管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public I18nMessageManager i18nMessageManager(I18nProperties properties, List<TranslationSource> sources) {
        Logs.info(EmojiSymbol.I18N, "载入模块：国际化，default={}, sources={}", properties.getDefaultLocale(),
                sources.size());
        return new I18nMessageManager(sources, properties.getDefaultLocale());
    }

    /**
     * 数据库翻译来源
     * <p>
     * {@link JdbcClient} 是可选依赖，因此条件注解用字符串形式判断，
     * 且只在容器确实存在该 bean 时才注册。
     */
    @ConditionalOnClass(name = "org.springframework.jdbc.core.simple.JdbcClient")
    @ConditionalOnBean(JdbcClient.class)
    @ConditionalOnProperty(prefix = I18nProperties.PREFIX, name = "jdbc-enabled", havingValue = "true",
            matchIfMissing = true)
    static class JdbcSourceConfiguration {

        /**
         * 数据库翻译源
         *
         * @param properties 配置
         * @param jdbcClient JDBC 客户端
         *
         * @return 翻译来源
         */
        @Bean
        @ConditionalOnMissingBean
        JdbcTranslationSource jdbcTranslationSource(I18nProperties properties, JdbcClient jdbcClient) {
            JdbcTranslationSource source = new JdbcTranslationSource(jdbcClient, properties.getTableName());
            if (properties.isSchemaInit()) {
                source.initSchema();
            }
            return source;
        }

    }

}
