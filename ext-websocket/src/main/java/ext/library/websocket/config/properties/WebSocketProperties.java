package ext.library.websocket.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

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

    /**
     * 表示发送单个消息的最大时间，默认 3 秒，单位：毫秒
     */
    Integer sendTimeLimit = 1000 * 3;

    /**
     * 表示发送消息的队列最大字节数，不是消息的数量而是消息的总大小，5MB
     */
    Integer bufferSizeLimit = 1024 * 1024 * 5;
}