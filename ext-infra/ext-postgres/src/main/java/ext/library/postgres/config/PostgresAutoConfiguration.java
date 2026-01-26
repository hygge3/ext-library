package ext.library.postgres.config;

import ext.library.postgres.cache.PostgresCacheManager;
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PostgresProperties.PREFIX, name = "auto-init-schema", havingValue = "true", matchIfMissing = true)
    public PostgresSchemaInitializer postgresSchemaInitializer(DataSource dataSource, PostgresProperties properties) {
        return new PostgresSchemaInitializer(dataSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresCacheManager postgresCacheManager(DataSource dataSource, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresCacheManager");
        return new PostgresCacheManager(dataSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresQueue postgresQueue(DataSource dataSource, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresQueue");
        return new PostgresQueue(dataSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresPubSub postgresPubSub(DataSource dataSource) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresPubSub");
        return new PostgresPubSub(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresRateLimiter postgresRateLimiter(DataSource dataSource, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresRateLimiter");
        return new PostgresRateLimiter(dataSource, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PostgresSessionManager postgresSessionManager(DataSource dataSource, PostgresProperties properties) {
        Logs.debug(EmojiSymbol.POSTGRES, "注册 Bean: PostgresSessionManager");
        return new PostgresSessionManager(dataSource, properties);
    }
}
