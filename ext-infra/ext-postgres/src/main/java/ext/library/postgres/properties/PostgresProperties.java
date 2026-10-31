package ext.library.postgres.properties;

import java.time.Duration;

/**
 * PostgreSQL 模块配置属性
 * <p>
 * 本类不带 {@code @ConfigurationProperties}，而是由
 * {@code PostgresAutoConfiguration#postgresProperties()} 这个具名 {@code @Bean} 方法完成绑定，
 * 以保证 bean 名为 {@code postgresProperties}，供 {@code #{@postgresProperties.xxx}} 形式的
 * {@code @Scheduled} SpEL 表达式引用。
 * <p>
 * 缓存 / 队列 / 限流三种能力各有自己的开关与表名（{@code ext.postgres.cache.*}、
 * {@code ext.postgres.queue.*}、{@code ext.postgres.ratelimit.*}），互不影响；
 * 发布订阅不需要表。
 *
 * @since 4.0.0
 */
public class PostgresProperties {

    public static final String PREFIX = "ext.postgres";

    /**
     * 数据源配置（可选，不配置则使用应用主数据源）
     */
    private DataSourceConfig datasource;

    /**
     * 缓存能力配置
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 队列能力配置
     */
    private QueueConfig queue = new QueueConfig();

    /**
     * 限流能力配置
     * <p>
     * 字段名刻意写成 {@code ratelimit}（一个词）以匹配配置前缀 {@code ext.postgres.ratelimit}——
     * Spring 的松散绑定不会把 {@code ratelimit} 拆成 {@code rate-limit}，写成 {@code rateLimit}
     * 反而绑不上。
     */
    private RateLimitConfig ratelimit = new RateLimitConfig();

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
     * 限流能力配置（计数表 + 固定窗口）
     */
    public static class RateLimitConfig {

        /**
         * 是否自动初始化限流表结构，关掉则完全不建表（由调用方自行用迁移工具建表）
         */
        private boolean autoInitSchema = true;

        /**
         * 限流表名
         */
        private String tableName = "pg_rate_limits";

        /**
         * 限流窗口大小
         */
        private Duration window = Duration.ofMinutes(1);

        /**
         * 默认限流阈值
         */
        private int defaultLimit = 100;

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

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }

        public int getDefaultLimit() {
            return defaultLimit;
        }

        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }

    }

    // region Getters and Setters

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

    public RateLimitConfig getRatelimit() {
        return ratelimit;
    }

    public void setRatelimit(RateLimitConfig ratelimit) {
        this.ratelimit = ratelimit;
    }

    // endregion

}
