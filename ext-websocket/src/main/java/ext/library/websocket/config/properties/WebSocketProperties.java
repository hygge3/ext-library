package ext.library.websocket.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * WebSocket 配置项
 */
@ConfigurationProperties(prefix = WebSocketProperties.PREFIX)
@Data
public class WebSocketProperties {

    public static final String PREFIX = "ext.websocket";

    /** 启用 */
    private Boolean enabled;

    /**
     * 路径
     */
    private String path;

    /**
     * 设置访问源地址
     */
    private String allowedOrigins;

}
