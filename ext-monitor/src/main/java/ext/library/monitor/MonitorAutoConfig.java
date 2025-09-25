package ext.library.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import oshi.SystemInfo;

/**
 * 系统监控限制配置
 */
@AutoConfiguration
public class MonitorAutoConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 创建并返回一个 OshiMonitor 实例，用于系统监控。
     *
     * @return OshiMonitor 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public OshiMonitor oshiMonitor() {
        log.info("[📊] 系统监控模块载入成功");
        return new OshiMonitor(new SystemInfo());
    }

}