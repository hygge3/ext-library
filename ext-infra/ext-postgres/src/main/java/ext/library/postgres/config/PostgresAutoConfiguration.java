package ext.library.postgres.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ext.library.postgres.cache.PostgresCacheManager;
import ext.library.postgres.properties.DataSourceConfig;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.postgres.pubsub.PostgresPubSub;
import ext.library.postgres.queue.PostgresQueue;
import ext.library.postgres.ratelimit.PostgresRateLimiter;
import ext.library.postgres.schema.PostgresSchemaInitializer;
import ext.library.postgres.session.PostgresSessionManager;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

/**
 * PostgreSQL 模块自动配置类
 *
 * @since 4.0.0
 */
@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(PostgresProperties.class)
@ConditionalOnClass(DataSource.class)
public class PostgresAutoConfiguration {

    public PostgresAutoConfiguration() {
        Logs.info(EmojiSymbol.POSTGRES, "载入模块: ext-postgres (PostgreSQL 缓存/队列/发布订阅/限流/会话)");
    }

    /**
     * 使用专用数据源的配置
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = PostgresProperties.PREFIX + ".datasource", name = "url")
    static class DedicatedDataSourceConfiguration {

        @Bean(name = "postgresDataSource", destroyMethod = "close")
        HikariDataSource postgresDataSource(PostgresProperties properties) {
            DataSourceConfig config = properties.getDatasource();
            Logs.info(EmojiSymbol.POSTGRES, "创建专用数据源: {}", config.getUrl());

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setDriverClassName(config.getDriverClassName());
            hikariConfig.setMinimumIdle(config.getMinimumIdle());
            hikariConfig.setMaximumPoolSize(config.getMaximumPoolSize());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout().toMillis());
            hikariConfig.setIdleTimeout(config.getIdleTimeout().toMillis());
            hikariConfig.setMaxLifetime(config.getMaxLifetime().toMillis());
            hikariConfig.setPoolName(config.getPoolName());

            return new HikariDataSource(hikariConfig);
        }

        @Bean(name = "postgresJdbcClient")
        JdbcClient postgresJdbcClient(HikariDataSource postgresDataSource) {
            return JdbcClient.create(postgresDataSource);
        }

        @Bean
        @ConditionalOnMissingBean
        PostgresPubSub postgresPubSub(HikariDataSource postgresDataSource) {
            Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresPubSub (专用数据源)");
            return new PostgresPubSub(postgresDataSource);
        }
    }

    /**
     * 使用应用主数据源的配置（回退方案）
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(name = "postgresDataSource")
    static class FallbackDataSourceConfiguration {

        @Bean(name = "postgresJdbcClient")
        @ConditionalOnMissingBean(name = "postgresJdbcClient")
        JdbcClient postgresJdbcClient(DataSource dataSource) {
            Logs.debug(EmojiSymbol.POSTGRES, "使用应用主数据源");
            return JdbcClient.create(dataSource);
        }

        @Bean
        @ConditionalOnMissingBean
        PostgresPubSub postgresPubSub(DataSource dataSource) {
            Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresPubSub (主数据源)");
            return new PostgresPubSub(dataSource);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PostgresProperties.PREFIX, name = "auto-init-schema", havingValue = "true", matchIfMissing = true)
    public PostgresSchemaInitializer postgresSchemaInitializer(JdbcClient postgresJdbcClient, PostgresProperties properties) {
        return new PostgresSchemaInitializer(postgresJdbcClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresCacheManager postgresCacheManager(JdbcClient postgresJdbcClient, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresCacheManager");
        return new PostgresCacheManager(postgresJdbcClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresQueue postgresQueue(JdbcClient postgresJdbcClient, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresQueue");
        return new PostgresQueue(postgresJdbcClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresRateLimiter postgresRateLimiter(JdbcClient postgresJdbcClient, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresRateLimiter");
        return new PostgresRateLimiter(postgresJdbcClient, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresSessionManager postgresSessionManager(JdbcClient postgresJdbcClient, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresSessionManager");
        return new PostgresSessionManager(postgresJdbcClient, properties);
    }
}
