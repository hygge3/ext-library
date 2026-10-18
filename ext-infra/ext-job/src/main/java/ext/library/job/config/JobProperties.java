package ext.library.job.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 任务执行配置
 * <p>
 * 前缀 {@code ext.job}
 *
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = JobProperties.PREFIX)
public class JobProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "ext.job";

    /**
     * 是否启用任务轮询
     */
    private boolean enabled = true;

    /**
     * 每轮每队列最大出队条数
     */
    private int batchSize = 10;

    /**
     * 轮询间隔（毫秒）
     * <p>
     * 声明为原始数值而非常用 Duration，因为 {@code @Scheduled} 只能引用配置占位符，
     * 避免与配置类再维护一份带单位的字段。
     */
    private long pollIntervalMillis = 500;

    /**
     * 启动时重置卡住任务的超时判定时长
     */
    private Duration stuckTimeout = Duration.ofMinutes(30);

    /**
     * 启动时清理已完成任务的保留时长
     */
    private Duration completedRetention = Duration.ofDays(7);

    /**
     * 是否启用启动恢复
     * <p>
     * 重置 {@code PROCESSING} 残留并清理已完成任务，多实例同时部署时应只在一个实例开启。
     */
    private boolean recoverOnStartup = true;

    /**
     * 是否启用启动时清理已完成任务
     */
    private boolean cleanupOnStartup = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getPollIntervalMillis() {
        return pollIntervalMillis;
    }

    public void setPollIntervalMillis(long pollIntervalMillis) {
        this.pollIntervalMillis = pollIntervalMillis;
    }

    public Duration getStuckTimeout() {
        return stuckTimeout;
    }

    public void setStuckTimeout(Duration stuckTimeout) {
        this.stuckTimeout = stuckTimeout;
    }

    public Duration getCompletedRetention() {
        return completedRetention;
    }

    public void setCompletedRetention(Duration completedRetention) {
        this.completedRetention = completedRetention;
    }

    public boolean isRecoverOnStartup() {
        return recoverOnStartup;
    }

    public void setRecoverOnStartup(boolean recoverOnStartup) {
        this.recoverOnStartup = recoverOnStartup;
    }

    public boolean isCleanupOnStartup() {
        return cleanupOnStartup;
    }

    public void setCleanupOnStartup(boolean cleanupOnStartup) {
        this.cleanupOnStartup = cleanupOnStartup;
    }

}
