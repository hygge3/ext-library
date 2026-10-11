package ext.library.postgres.schema;

import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * PostgreSQL 表结构初始化器
 * <p>
 * 在应用启动时自动创建所需的表结构
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
        // 创建缓存表
        createCacheTable();

        // 创建队列表
        createQueueTable();

        // 创建限流表
        createRateLimitTable();

        Logs.info(EmojiSymbol.POSTGRES, "PostgreSQL 表结构初始化完成");
    }

    /**
     * 创建缓存表 (UNLOGGED)
     */
    private void createCacheTable() {
        String tableName = properties.getCacheTableName();

        // 创建表
        jdbcClient.sql("""
                        CREATE UNLOGGED TABLE IF NOT EXISTS %s (
                            key TEXT PRIMARY KEY,
                            value JSONB NOT NULL,
                            expires_at TIMESTAMPTZ NOT NULL,
                            created_at TIMESTAMPTZ DEFAULT NOW(),
                            updated_at TIMESTAMPTZ DEFAULT NOW()
                        )
                        """.formatted(tableName))
                .update();

        // 创建过期时间索引
        jdbcClient.sql("CREATE INDEX IF NOT EXISTS idx_%s_expires ON %s(expires_at)"
                        .formatted(tableName, tableName))
                .update();

        // 创建更新时间触发器函数
        jdbcClient.sql("""
                        CREATE OR REPLACE FUNCTION update_%s_updated_at()
                        RETURNS TRIGGER AS $$
                        BEGIN
                            NEW.updated_at = NOW();
                            RETURN NEW;
                        END;
                        $$ LANGUAGE plpgsql
                        """.formatted(tableName))
                .update();

        // 删除旧触发器并创建新触发器
        jdbcClient.sql("DROP TRIGGER IF EXISTS trg_%s_updated_at ON %s"
                        .formatted(tableName, tableName))
                .update();

        jdbcClient.sql("""
                        CREATE TRIGGER trg_%s_updated_at
                            BEFORE UPDATE ON %s
                            FOR EACH ROW
                            EXECUTE FUNCTION update_%s_updated_at()
                        """.formatted(tableName, tableName, tableName))
                .update();

        Logs.debug(EmojiSymbol.POSTGRES, "缓存表 {} 初始化完成", tableName);
    }

    /**
     * 创建队列表
     */
    private void createQueueTable() {
        String tableName = properties.getQueueTableName();

        // 创建表
        jdbcClient.sql("""
                        CREATE TABLE IF NOT EXISTS %s (
                            id BIGSERIAL PRIMARY KEY,
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
                        """.formatted(tableName))
                .update();

        // 创建待处理任务索引 (核心性能优化)
        jdbcClient.sql("""
                        CREATE INDEX IF NOT EXISTS idx_%s_pending ON %s(queue, scheduled_at)
                            WHERE status = 'PENDING' AND attempts < max_attempts
                        """.formatted(tableName, tableName))
                .update();

        // 创建状态索引
        jdbcClient.sql("CREATE INDEX IF NOT EXISTS idx_%s_status ON %s(queue, status)"
                        .formatted(tableName, tableName))
                .update();

        Logs.debug(EmojiSymbol.POSTGRES, "队列表 {} 初始化完成", tableName);
    }

    /**
     * 创建限流表
     */
    private void createRateLimitTable() {
        String tableName = properties.getRateLimitTableName();

        // 创建表
        jdbcClient.sql("""
                        CREATE TABLE IF NOT EXISTS %s (
                            key TEXT PRIMARY KEY,
                            request_count INT DEFAULT 0,
                            window_start TIMESTAMPTZ DEFAULT NOW()
                        )
                        """.formatted(tableName))
                .update();

        // 创建窗口过期索引
        jdbcClient.sql("CREATE INDEX IF NOT EXISTS idx_%s_window ON %s(window_start)"
                        .formatted(tableName, tableName))
                .update();

        Logs.debug(EmojiSymbol.POSTGRES, "限流表 {} 初始化完成", tableName);
    }

}
