package ext.library.monitor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import oshi.SystemInfo;

/**
 * 系统监控限制配置
 */
@AutoConfiguration
public class MonitorAutoConfig {

    /**
     * 创建并返回一个 OshiMonitor 实例，用于系统监控。
     *
     * @return OshiMonitor 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public OshiMonitor oshiMonitor() {
        return new OshiMonitor(new SystemInfo());
    }

}