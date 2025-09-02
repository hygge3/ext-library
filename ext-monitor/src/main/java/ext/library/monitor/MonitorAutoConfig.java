package ext.library.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import oshi.SystemInfo;

/**
 * 系统监控限制配置
 */
@Slf4j
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
        log.info("[📊] 系统监控模块载入成功");
        return new OshiMonitor(new SystemInfo());
    }

}