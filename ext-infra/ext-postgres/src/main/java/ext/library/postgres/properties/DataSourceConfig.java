package ext.library.postgres.properties;

import java.time.Duration;

/**
 * PostgreSQL 数据源配置
 * <p>
 * 用于配置 ext-postgres 模块专用的数据源，支持独立于应用主数据源运行
 *
 * @since 4.0.0
 */
public class DataSourceConfig {

    /**
     * JDBC URL
     * <p>
     * 示例: jdbc:postgresql://localhost:5432/mydb
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 驱动类名（可选，默认自动检测）
     */
    private String driverClassName = "org.postgresql.Driver";

    /**
     * 连接池最小空闲连接数
     */
    private int minimumIdle = 2;

    /**
     * 连接池最大连接数
     */
    private int maximumPoolSize = 10;

    /**
     * 连接超时时间
     */
    private Duration connectionTimeout = Duration.ofSeconds(30);

    /**
     * 空闲连接超时时间
     */
    private Duration idleTimeout = Duration.ofMinutes(10);

    /**
     * 连接最大生命周期
     */
    private Duration maxLifetime = Duration.ofMinutes(30);

    /**
     * 连接池名称
     */
    private String poolName = "ext-postgres-pool";

    // region Getters and Setters

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(int minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Duration getMaxLifetime() {
        return maxLifetime;
    }

    public void setMaxLifetime(Duration maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    // endregion

    /**
     * 检查数据源配置是否有效
     *
     * @return 如果 URL 已配置则返回 true
     */
    public boolean isConfigured() {
        return url != null && !url.isBlank();
    }
}
