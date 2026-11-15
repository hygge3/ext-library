package ext.library.accesslog.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 访问日志配置
 */
@Data
@ConfigurationProperties(prefix = AccessLogProperties.PREFIX)
public class AccessLogProperties {

    public static final String PREFIX = "ext.accesslog";

    /** 是否启用访问日志 */
    private boolean enabled = true;

    /** 是否记录查询参数 */
    private boolean includeQueryString = true;

    /** 是否记录请求头（敏感头会被过滤） */
    private boolean includeHeaders = false;

    /** 是否记录 User-Agent */
    private boolean includeUserAgent = true;

    /** 是否记录客户端 IP */
    private boolean includeClientIp = true;

    /** 慢请求阈值（毫秒），超过则以 WARN 级别记录；<=0 不启用慢请求标记 */
    private long slowThresholdMs = 3000;

    /** 排除的路径模式（前缀或 Ant 风格） */
    private List<String> excludePatterns = new ArrayList<>(List.of(
            "/actuator", "/favicon.ico", "/error", "/swagger", "/v3/api-docs", "/webjars"
    ));
}
