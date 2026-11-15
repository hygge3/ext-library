package ext.library.postgres.schema;

import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL 表结构初始化器
 * <p>
 * 在应用启动时为「开着 {@code auto-init-schema}」的能力建表：缓存（{@code ext.postgres.cache.*}）、
 * 队列（{@code ext.postgres.queue.*}）、令牌桶（{@code ext.postgres.bucket.*}）。发布订阅无需建表。
 * <p>
 * 关掉某个能力就完全不给它建表——调用方自己用迁移工具（Flyway / Liquibase）建表即可，
 * 下游模块（如 ext-job）也不会替它补表。三个都关时连库都不会碰。
 * <p>
 * 配了 {@code ext.postgres.schema} 时先 {@code CREATE SCHEMA IF NOT EXISTS}，需要建 schema 的权限；
 * 没有该权限就自己先建好 schema，把这一句跳过。
 *
 * @since 4.0.0
 */
public class PostgresSchemaInitializer implements InitializingBean {

    private final JdbcClient jdbcClient;
    private final PostgresProperties properties;

    public PostgresSchemaInitializer(JdbcClient jdbcClient, PostgresProperties properties) {
        this.jdbcClient = jdbcClient;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        initSchema();
    }

    /**
     * 初始化表结构
     */
    public void initSchema() {
        List<String> statements = statements(properties);
        if (statements.isEmpty()) {
            Logs.info(EmojiSymbol.POSTGRES, "所有能力的 auto-init-schema 均已关闭，跳过表结构初始化（表需由调用方自行创建）");
            return;
        }
        for (String statement : statements) {
            jdbcClient.sql(statement).update();
        }
        Logs.info(EmojiSymbol.POSTGRES, "PostgreSQL 表结构初始化完成：schema={}",
                properties.getSchema().isBlank() ? "search_path" : properties.getSchema());
    }

    /**
     * 生成本次要执行的 DDL
     * <p>
     * 索引名、触发器名、函数名都是标识符，只有表引用才带 schema 前缀——把 {@code middle.pg_cache}
     * 直接拼进 {@code idx_} 里会得到 {@code idx_middle.pg_cache_expires} 这种非法标识符。
     *
     * @param properties 模块配置
     *
     * @return 按执行顺序排列的语句，三个能力全关时为空
     */
    static List<String> statements(PostgresProperties properties) {
        PostgresProperties.CacheConfig cache = properties.getCache();
        PostgresProperties.QueueConfig queue = properties.getQueue();
        PostgresProperties.BucketConfig bucket = properties.getBucket();

        List<String> statements = new ArrayList<>();
        if (properties.getSchema() != null && !properties.getSchema().isBlank()) {
            statements.add("CREATE SCHEMA IF NOT EXISTS " + properties.getSchema());
        }
        if (cache.isAutoInitSchema()) {
            statements.addAll(cacheStatements(properties, cache.getTableName()));
        }
        if (queue.isAutoInitSchema()) {
            statements.addAll(queueStatements(properties, queue.getTableName()));
        }
        if (bucket.isAutoInitSchema()) {
            statements.addAll(bucketStatements(properties, bucket.getTableName()));
        }
        return statements;
    }

    /**
     * 创建令牌桶表
     * <p>
     * 不加过期时间列：桶的 TTL 不是行级关注点，过期行留着也无害（tat 落后于 NOW() 等于满桶）。
     */
    private static List<String> bucketStatements(PostgresProperties properties, String tableName) {
        return List.of("""
                CREATE UNLOGGED TABLE IF NOT EXISTS %s (
                    key TEXT PRIMARY KEY,
                    tat TIMESTAMPTZ NOT NULL
                )
                """.formatted(properties.qualify(tableName)));
    }

    /**
     * 创建缓存表（UNLOGGED）与过期时间索引、更新时间触发器
     */
    private static List<String> cacheStatements(PostgresProperties properties, String tableName) {
        String table = properties.qualify(tableName);
        String updatedAtFunction = properties.qualify("update_" + tableName + "_updated_at");
        return List.of(
                """
                CREATE UNLOGGED TABLE IF NOT EXISTS %s (
                    key TEXT PRIMARY KEY,
                    value JSONB NOT NULL,
                    expires_at TIMESTAMPTZ NOT NULL,
                    created_at TIMESTAMPTZ DEFAULT NOW(),
                    updated_at TIMESTAMPTZ DEFAULT NOW()
                )
                """.formatted(table),
                "CREATE INDEX IF NOT EXISTS idx_%s_expires ON %s(expires_at)".formatted(tableName, table),
                """
                CREATE OR REPLACE FUNCTION %s()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql
                """.formatted(updatedAtFunction),
                "DROP TRIGGER IF EXISTS trg_%s_updated_at ON %s".formatted(tableName, table),
                """
                CREATE TRIGGER trg_%s_updated_at
                    BEFORE UPDATE ON %s
                    FOR EACH ROW
                    EXECUTE FUNCTION %s()
                """.formatted(tableName, table, updatedAtFunction));
    }

    /**
     * 创建队列表与两个索引
     */
    private static List<String> queueStatements(PostgresProperties properties, String tableName) {
        String table = properties.qualify(tableName);
        return List.of(
                """
                CREATE TABLE IF NOT EXISTS %s (
                    id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                    queue TEXT NOT NULL,
                    payload JSONB NOT NULL,
                    status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
                    attempts INT DEFAULT 0,
                    max_attempts INT DEFAULT 3,
                    error TEXT,
                    scheduled_at TIMESTAMPTZ DEFAULT NOW(),
                    started_at TIMESTAMPTZ,
                    completed_at TIMESTAMPTZ,
                    created_at TIMESTAMPTZ DEFAULT NOW()
                )
                """.formatted(table),
                // 待处理任务索引：轮询只扫 PENDING 且未超次数的行
                """
                CREATE INDEX IF NOT EXISTS idx_%s_pending ON %s(queue, scheduled_at)
                    WHERE status = 'PENDING' AND attempts < max_attempts
                """.formatted(tableName, table),
                "CREATE INDEX IF NOT EXISTS idx_%s_status ON %s(queue, status)".formatted(tableName, table));
    }
}
