package ext.library.postgres.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * PostgreSQL 模块配置属性
 *
 * @since 4.0.0
 */
@ConfigurationProperties(prefix = PostgresProperties.PREFIX)
public class PostgresProperties {

    public static final String PREFIX = "ext.postgres";

    /**
     * 是否启用模块
     */
    private boolean enabled = true;

    /**
     * 是否自动初始化表结构
     */
    private boolean autoInitSchema = true;

    /**
     * 缓存表名
     */
    private String cacheTableName = "pg_cache";

    /**
     * 队列表名
     */
    private String queueTableName = "pg_jobs";

    /**
     * 限流表名
     */
    private String rateLimitTableName = "pg_rate_limits";

    /**
     * 默认缓存过期时间
     */
    private Duration defaultCacheExpireTime = Duration.ofHours(1);

    /**
     * 缓存清理间隔
     */
    private Duration cacheCleanupInterval = Duration.ofMinutes(5);

    /**
     * 队列默认最大重试次数
     */
    private int queueMaxAttempts = 3;

    /**
     * 队列轮询间隔
     */
    private Duration queuePollInterval = Duration.ofMillis(100);

    /**
     * 限流窗口大小
     */
    private Duration rateLimitWindow = Duration.ofMinutes(1);

    /**
     * 默认限流阈值
     */
    private int defaultRateLimit = 100;

    /**
     * 会话表名
     */
    private String sessionTableName = "pg_sessions";

    /**
     * 默认会话过期时间
     */
    private Duration defaultSessionTimeout = Duration.ofHours(24);

    /**
     * 会话活动超时时间（无活动后过期）
     */
    private Duration sessionActivityTimeout = Duration.ofMinutes(30);

    /**
     * 会话清理间隔
     */
    private Duration sessionCleanupInterval = Duration.ofMinutes(10);

    // region Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAutoInitSchema() {
        return autoInitSchema;
    }

    public void setAutoInitSchema(boolean autoInitSchema) {
        this.autoInitSchema = autoInitSchema;
    }

    public String getCacheTableName() {
        return cacheTableName;
    }

    public void setCacheTableName(String cacheTableName) {
        this.cacheTableName = cacheTableName;
    }

    public String getQueueTableName() {
        return queueTableName;
    }

    public void setQueueTableName(String queueTableName) {
        this.queueTableName = queueTableName;
    }

    public String getRateLimitTableName() {
        return rateLimitTableName;
    }

    public void setRateLimitTableName(String rateLimitTableName) {
        this.rateLimitTableName = rateLimitTableName;
    }

    public Duration getDefaultCacheExpireTime() {
        return defaultCacheExpireTime;
    }

    public void setDefaultCacheExpireTime(Duration defaultCacheExpireTime) {
        this.defaultCacheExpireTime = defaultCacheExpireTime;
    }

    public Duration getCacheCleanupInterval() {
        return cacheCleanupInterval;
    }

    public void setCacheCleanupInterval(Duration cacheCleanupInterval) {
        this.cacheCleanupInterval = cacheCleanupInterval;
    }

    public int getQueueMaxAttempts() {
        return queueMaxAttempts;
    }

    public void setQueueMaxAttempts(int queueMaxAttempts) {
        this.queueMaxAttempts = queueMaxAttempts;
    }

    public Duration getQueuePollInterval() {
        return queuePollInterval;
    }

    public void setQueuePollInterval(Duration queuePollInterval) {
        this.queuePollInterval = queuePollInterval;
    }

    public Duration getRateLimitWindow() {
        return rateLimitWindow;
    }

    public void setRateLimitWindow(Duration rateLimitWindow) {
        this.rateLimitWindow = rateLimitWindow;
    }

    public int getDefaultRateLimit() {
        return defaultRateLimit;
    }

    public void setDefaultRateLimit(int defaultRateLimit) {
        this.defaultRateLimit = defaultRateLimit;
    }

    public String getSessionTableName() {
        return sessionTableName;
    }

    public void setSessionTableName(String sessionTableName) {
        this.sessionTableName = sessionTableName;
    }

    public Duration getDefaultSessionTimeout() {
        return defaultSessionTimeout;
    }

    public void setDefaultSessionTimeout(Duration defaultSessionTimeout) {
        this.defaultSessionTimeout = defaultSessionTimeout;
    }

    public Duration getSessionActivityTimeout() {
        return sessionActivityTimeout;
    }

    public void setSessionActivityTimeout(Duration sessionActivityTimeout) {
        this.sessionActivityTimeout = sessionActivityTimeout;
    }

    public Duration getSessionCleanupInterval() {
        return sessionCleanupInterval;
    }

    public void setSessionCleanupInterval(Duration sessionCleanupInterval) {
        this.sessionCleanupInterval = sessionCleanupInterval;
    }

    // endregion
}
