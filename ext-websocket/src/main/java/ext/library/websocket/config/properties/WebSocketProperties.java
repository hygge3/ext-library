package ext.library.websocket.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

/**
 * WebSocket 配置项
 */
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public Integer getSendTimeLimit() {
        return sendTimeLimit;
    }

    public void setSendTimeLimit(Integer sendTimeLimit) {
        this.sendTimeLimit = sendTimeLimit;
    }

    public Integer getBufferSizeLimit() {
        return bufferSizeLimit;
    }

    public void setBufferSizeLimit(Integer bufferSizeLimit) {
        this.bufferSizeLimit = bufferSizeLimit;
    }
}