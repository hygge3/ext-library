package ext.library.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 操作审计配置属性
 */
@ConfigurationProperties(AuditProperties.PREFIX)
public class AuditProperties {

    public static final String PREFIX = "ext.audit";

    /**
     * 未注册任何 {@link ext.library.audit.AuditHandler} 时，
     * 是否把审计记录降级打到控制台日志
     */
    private boolean logToConsole = true;

    public boolean isLogToConsole() {
        return logToConsole;
    }

    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }

}
