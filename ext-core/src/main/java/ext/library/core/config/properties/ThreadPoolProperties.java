package ext.library.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 线程池 配置属性
 */
@Data
@ConfigurationProperties(prefix = "thread-pool")
public class ThreadPoolProperties {

    /**
     * 是否开启自定义线程池
     */
     boolean enabled;

    /**
     * 队列最大长度
     */
     int queueCapacity;

    /**
     * 线程池维护线程所允许的空闲时间
     */
     int keepAliveSeconds;

}
