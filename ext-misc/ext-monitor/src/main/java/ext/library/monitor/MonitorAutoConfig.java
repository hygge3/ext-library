package ext.library.monitor;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 系统监控自动配置
 */
@AutoConfiguration
public class MonitorAutoConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 创建系统监控器实例
     *
     * @return SystemMonitor 实例
     */
    @Bean
    @ConditionalOnMissingBean
    public SystemMonitor systemMonitor() {
        Logs.info(EmojiSymbol.MONITOR, "载入模块：系统监控");
        return new SystemMonitor();
    }
}
