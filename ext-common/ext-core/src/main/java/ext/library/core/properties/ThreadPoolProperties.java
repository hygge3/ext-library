package ext.library.core.properties;

import ext.library.tool.runtime.Runtimes;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池配置属性
 */
@ConfigurationProperties(prefix = "ext.thread-pool")
public class ThreadPoolProperties {

    /** 是否开启自定义线程池 */
    private boolean enabled;

    /** 核心线程数 (默认: CPU核数 + 1) */
    private int corePoolSize = Runtimes.getCpuNum() + 1;

    /**
     * 最大线程数，<= 0 时跟随 {@code corePoolSize * 2}
     * <p>
     * 不能在字段初始化时写死 {@code corePoolSize * 2}：那是用默认 core 值算出来的，
     * 用户只配 {@code corePoolSize=64} 时 max 仍是 cpu*2，core > max 直接启动失败。
     */
    private int maxPoolSize;

    /** 队列最大长度 (默认: 200) */
    private int queueCapacity = 200;

    /** 线程空闲存活时间，单位秒 (默认: 60) */
    private int keepAliveSeconds = 60;

    /** 关闭时等待在途任务完成的最长秒数 (默认: 30)，仅在 waitForTasksToCompleteOnShutdown 开启时生效 */
    private int awaitTerminationSeconds = 30;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize > 0 ? maxPoolSize : corePoolSize * 2;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public int getAwaitTerminationSeconds() {
        return awaitTerminationSeconds;
    }

    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

}
