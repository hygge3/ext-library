package ext.library.websocket.config.properties;

import jakarta.validation.constraints.Pattern;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * WebSocket 配置项
 */
@Data
@ConfigurationProperties(prefix = WebSocketProperties.PREFIX)
@Validated
public class WebSocketProperties {

    public static final String PREFIX = "ext.websocket";

    /** 启用 */
    Boolean enabled;

    /**
     * 路径
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    String path;

    /**
     * 设置访问源地址
     */
    String allowedOrigins;

}
