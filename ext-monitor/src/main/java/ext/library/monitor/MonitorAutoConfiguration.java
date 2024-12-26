package ext.library.monitor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import oshi.SystemInfo;

/**
 * 系统监控限制配置
 */
@AutoConfiguration
public class MonitorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OshiMonitor oshiMonitor() {
        return new OshiMonitor(new SystemInfo());
    }

}
