package ext.library.websocket.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

/**
 * WebSocket 配置项
 */
@Getter
@Setter
@ConfigurationProperties(prefix = WebSocketProperties.PREFIX)
@Validated
public class WebSocketProperties {

    public static final String PREFIX = "ext.websocket";

    /** 启用 */
    private Boolean enabled;

    /**
     * 路径
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    private String path;

    /**
     * 设置访问源地址
     */
    private String allowedOrigins;

    /**
     * 表示发送单个消息的最大时间，默认 3 秒，单位：毫秒
     */
    private Integer sendTimeLimit = 1000 * 3;

    /**
     * 表示发送消息的队列最大字节数，不是消息的数量而是消息的总大小，5MB
     */
    private Integer bufferSizeLimit = 1024 * 1024 * 5;
}