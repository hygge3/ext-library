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

    /** 最大线程数 (默认: 核心线程数 * 2) */
    private int maxPoolSize = corePoolSize * 2;

    /** 队列最大长度 (默认: 200) */
    private int queueCapacity = 200;

    /** 线程空闲存活时间，单位秒 (默认: 60) */
    private int keepAliveSeconds = 60;

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
        return maxPoolSize;
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

}
