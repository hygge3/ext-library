package ext.library.trace.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 链路追踪配置
 */
@Data
@ConfigurationProperties(prefix = TraceProperties.PREFIX)
public class TraceProperties {

    public static final String PREFIX = "ext.trace";

    /** 是否启用追踪过滤器 */
    private boolean enabled = true;

    /** MDC 中的追踪 ID 键名 */
    private String traceIdMdcKey = "traceId";

    /** MDC 中的请求 ID 键名（与 traceId 相同值时可用于兼容） */
    private String requestIdMdcKey = "requestId";

    /** 请求头名称（按顺序尝试读取） */
    private List<String> requestIdHeaders = new ArrayList<>(List.of("X-Request-Id", "X-Trace-Id", "traceparent"));

    /** 是否把追踪 ID 写回响应头 */
    private boolean responseHeaderEnabled = true;

    /** 响应头名称 */
    private String responseHeaderName = "X-Request-Id";

    /** 需要排除的路径模式（Ant 风格由过滤器简单匹配前缀/包含） */
    private List<String> excludePatterns = new ArrayList<>(List.of(
            "/actuator", "/favicon.ico", "/error", "/swagger", "/v3/api-docs", "/webjars"
    ));

    /** 是否为线程池/异步任务传播 MDC */
    private boolean propagateMdc = true;
}
