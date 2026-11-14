package ext.library.metrics.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 指标与告警配置
 */
@ConfigurationProperties(prefix = MetricsProperties.PREFIX)
public class MetricsProperties {

    public static final String PREFIX = "ext.metrics";

    /**
     * 是否启用指标采集
     */
    private boolean enabled = true;

    /**
     * 统计窗口长度
     * <p>
     * QPS、错误率、P99、错误码分布都按该窗口累计，窗口到期清零。
     */
    private Duration window = Duration.ofSeconds(60);

    /**
     * 端点维度上限
     * <p>
     * 超出后新端点统一归入 {@code other}，避免路径带 ID 时把内存打爆。
     */
    private int maxEndpoints = 200;

    /**
     * 慢 SQL 阈值
     */
    private Duration slowSqlThreshold = Duration.ofMillis(500);

    /**
     * 保留的慢 SQL 条数
     */
    private int maxSlowSql = 20;

    /**
     * 慢 SQL 文本最大长度，超出截断
     */
    private int maxSqlLength = 512;

    /**
     * HTTP 埋点排除路径
     */
    private String[] excludePatterns = {"/actuator", "/favicon.ico", "/error", "/swagger", "/v3/api-docs", "/webjars", "/metrics"};

    private final Pool pool = new Pool();

    private final Alert alert = new Alert();

    private final Dashboard dashboard = new Dashboard();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getWindow() {
        return window;
    }

    public void setWindow(Duration window) {
        this.window = window;
    }

    public int getMaxEndpoints() {
        return maxEndpoints;
    }

    public void setMaxEndpoints(int maxEndpoints) {
        this.maxEndpoints = maxEndpoints;
    }

    public Duration getSlowSqlThreshold() {
        return slowSqlThreshold;
    }

    public void setSlowSqlThreshold(Duration slowSqlThreshold) {
        this.slowSqlThreshold = slowSqlThreshold;
    }

    public int getMaxSlowSql() {
        return maxSlowSql;
    }

    public void setMaxSlowSql(int maxSlowSql) {
        this.maxSlowSql = maxSlowSql;
    }

    public int getMaxSqlLength() {
        return maxSqlLength;
    }

    public void setMaxSqlLength(int maxSqlLength) {
        this.maxSqlLength = maxSqlLength;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(String[] excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public Pool getPool() {
        return pool;
    }

    public Alert getAlert() {
        return alert;
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    /**
     * 池水位采样配置
     */
    public static class Pool {

        /**
         * 是否采样线程池与连接池水位
         */
        private boolean enabled = true;

        /**
         * 采样间隔
         */
        private Duration interval = Duration.ofSeconds(15);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }
    }

    /**
     * 告警配置
     */
    public static class Alert {

        /**
         * 是否启用告警评估
         */
        private boolean enabled = true;

        /**
         * 评估间隔
         */
        private Duration interval = Duration.ofSeconds(10);

        /**
         * 触发告警的最小请求数
         * <p>
         * 样本太少时错误率与分位都没有意义，低流量接口容易误报。
         */
        private long minRequests = 20;

        /**
         * 错误率阈值（5xx 占比）
         */
        private double errorRateThreshold = 0.01;

        /**
         * P99 延迟阈值
         */
        private Duration p99Threshold = Duration.ofMillis(500);

        /**
         * 同一告警的冷却时间
         */
        private Duration cooldown = Duration.ofMinutes(5);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getInterval() {
            return interval;
        }

        public void setInterval(Duration interval) {
            this.interval = interval;
        }

        public long getMinRequests() {
            return minRequests;
        }

        public void setMinRequests(long minRequests) {
            this.minRequests = minRequests;
        }

        public double getErrorRateThreshold() {
            return errorRateThreshold;
        }

        public void setErrorRateThreshold(double errorRateThreshold) {
            this.errorRateThreshold = errorRateThreshold;
        }

        public Duration getP99Threshold() {
            return p99Threshold;
        }

        public void setP99Threshold(Duration p99Threshold) {
            this.p99Threshold = p99Threshold;
        }

        public Duration getCooldown() {
            return cooldown;
        }

        public void setCooldown(Duration cooldown) {
            this.cooldown = cooldown;
        }
    }

    /**
     * 大屏配置
     */
    public static class Dashboard {

        /**
         * 是否注册大屏
         * <p>
         * 默认开启，跟「引入依赖即接入」保持一致。取数据的接口里有慢 SQL 与端点清单，
         * 访问控制见 {@link #token}。
         */
        private boolean enabled = true;

        /**
         * 访问令牌
         * <p>
         * 配置后请求必须带 {@code X-Metrics-Token} 头或 {@code ?token=} 参数；不配置时只允许本机
         * （{@code 127.0.0.1} / {@code ::1}）访问——默认把慢 SQL 暴露给整个内网等于公开表结构。
         */
        private String token = "";

        /**
         * 挂载路径，{@code /metrics} 为页面，{@code /metrics/snapshot} 为数据
         */
        private String path = "/metrics";

        /**
         * 页面刷新间隔（秒）
         */
        private int refreshSeconds = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public int getRefreshSeconds() {
            return refreshSeconds;
        }

        public void setRefreshSeconds(int refreshSeconds) {
            this.refreshSeconds = Math.max(1, refreshSeconds);
        }
    }
}
