package ext.library.postgres.properties;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * PostgreSQL 模块配置属性
 * <p>
 * 本类不带 {@code @ConfigurationProperties}，而是由
 * {@code PostgresAutoConfiguration#postgresProperties()} 这个具名 {@code @Bean} 方法完成绑定，
 * 以保证 bean 名为 {@code postgresProperties}，供 {@code #{@postgresProperties.xxx}} 形式的
 * {@code @Scheduled} SpEL 表达式引用。
 * <p>
 * 缓存 / 队列 / 令牌桶三种能力各有自己的开关与表名（{@code ext.postgres.cache.*}、
 * {@code ext.postgres.queue.*}、{@code ext.postgres.bucket.*}），互不影响；
 * 发布订阅不需要表。
 *
 * @since 4.0.0
 */
public class PostgresProperties {

    public static final String PREFIX = "ext.postgres";

    private static final Pattern SCHEMA_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * 数据源配置（可选，不配置则使用应用主数据源）
     */
    private DataSourceConfig datasource;

    /**
     * 三种能力建表 / 查询所用的 schema，留空则沿用连接上的 {@code search_path}
     */
    private String schema = "";

    /**
     * 缓存能力配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 队列能力配置
     */
    private QueueConfig queue = new QueueConfig();

    /**
     * 令牌桶能力配置
     */
    private BucketConfig bucket = new BucketConfig();

    /**
     * 缓存能力配置（UNLOGGED 表）
     */
    public static class CacheConfig {

        /**
         * 是否自动初始化缓存的表结构，关掉则完全不建表（由调用方自行用迁移工具建表）
         */
        private boolean autoInitSchema = true;

        /**
         * 缓存表名
         */
        private String tableName = "pg_cache";

        /**
         * 默认缓存过期时间
         */
        private Duration defaultExpireTime = Duration.ofHours(1);

        /**
         * 过期缓存清理间隔
         */
        private Duration cleanupInterval = Duration.ofMinutes(5);

        public boolean isAutoInitSchema() {
            return autoInitSchema;
        }

        public void setAutoInitSchema(boolean autoInitSchema) {
            this.autoInitSchema = autoInitSchema;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public Duration getDefaultExpireTime() {
            return defaultExpireTime;
        }

        public void setDefaultExpireTime(Duration defaultExpireTime) {
            this.defaultExpireTime = defaultExpireTime;
        }

        public Duration getCleanupInterval() {
            return cleanupInterval;
        }

        public void setCleanupInterval(Duration cleanupInterval) {
            this.cleanupInterval = cleanupInterval;
        }

    }

    /**
     * 队列能力配置（{@code FOR UPDATE SKIP LOCKED}）
     */
    public static class QueueConfig {

        /**
         * 是否自动初始化队列表结构，关掉则完全不建表（由调用方自行用迁移工具建表）
         */
        private boolean autoInitSchema = true;

        /**
         * 队列表名
         */
        private String tableName = "pg_jobs";

        /**
         * 任务默认最大重试次数
         */
        private int maxAttempts = 3;

        public boolean isAutoInitSchema() {
            return autoInitSchema;
        }

        public void setAutoInitSchema(boolean autoInitSchema) {
            this.autoInitSchema = autoInitSchema;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

    }

    /**
     * 令牌桶能力配置（限流用，每行存一个 GCRA 理论到达时间）
     */
    public static class BucketConfig {

        /**
         * 是否自动初始化令牌桶表结构，关掉则完全不建表（由调用方自行用迁移工具建表）
         */
        private boolean autoInitSchema = true;

        /**
         * 令牌桶表名
         */
        private String tableName = "pg_token_bucket";

        public boolean isAutoInitSchema() {
            return autoInitSchema;
        }

        public void setAutoInitSchema(boolean autoInitSchema) {
            this.autoInitSchema = autoInitSchema;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

    }

    /**
     * 给表名拼上 schema 前缀
     * <p>
     * 预建表建在非默认 schema 下时，把 {@link #setSchema(String)} 填成该 schema 即可，各能力的
     * {@code table-name} 仍写裸表名。
     *
     * @param tableName 裸表名
     *
     * @return 带 schema 前缀的表名
     */
    public String qualify(String tableName) {
        return schema == null || schema.isBlank() ? tableName : schema + "." + tableName;
    }

    // region Getters and Setters

    /**
     * 设置 schema
     * <p>
     * 只接受普通标识符：表名会被直接拼进 SQL，不允许引号、分号、点号等能改变语句结构的东西。
     *
     * @param schema schema 名，空串表示沿用 {@code search_path}
     */
    public void setSchema(String schema) {
        if (schema != null && !schema.isBlank() && !SCHEMA_PATTERN.matcher(schema).matches()) {
            throw new IllegalArgumentException("ext.postgres.schema 只接受普通标识符：" + schema);
        }
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    public DataSourceConfig getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSourceConfig datasource) {
        this.datasource = datasource;
    }

    public CacheConfig getCache() {
        return cache;
    }

    public void setCache(CacheConfig cache) {
        this.cache = cache;
    }

    public QueueConfig getQueue() {
        return queue;
    }

    public void setQueue(QueueConfig queue) {
        this.queue = queue;
    }

    public BucketConfig getBucket() {
        return bucket;
    }

    public void setBucket(BucketConfig bucket) {
        this.bucket = bucket;
    }

    // endregion

}
