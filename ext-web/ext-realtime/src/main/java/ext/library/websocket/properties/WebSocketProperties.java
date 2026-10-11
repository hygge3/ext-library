package ext.library.websocket.properties;

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

    /**
     * 默认发布订阅主题
     */
    public static final String DEFAULT_TOPIC = "ext:websocket";

    /**
     * 启用 WebSocket 功能
     */
    private Boolean enabled;

    /**
     * WebSocket 端点路径
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    private String path = "/websocket";

    /**
     * 允许跨域访问的地址
     */
    private String allowedOrigins = "*";

    /**
     * 发送单个消息的最大时间，单位：毫秒
     */
    private Integer sendTimeLimit = 3000;

    /**
     * 发送消息队列的最大字节数（5MB）
     */
    private Integer bufferSizeLimit = 1024 * 1024 * 5;

    /**
     * 发布订阅主题，用于跨服务器消息推送
     */
    private String topic = DEFAULT_TOPIC;

    /**
     * 发布订阅后端类型
     */
    private PubSubBackend backend = PubSubBackend.REDIS;

    /**
     * 心跳配置
     */
    private Heartbeat heartbeat = new Heartbeat();

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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public PubSubBackend getBackend() {
        return backend;
    }

    public void setBackend(PubSubBackend backend) {
        this.backend = backend;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    /**
     * 发布订阅后端类型
     */
    public enum PubSubBackend {
        /**
         * 使用 Redis 发布订阅
         */
        REDIS,
        /**
         * 使用 PostgreSQL LISTEN/NOTIFY
         */
        POSTGRES
    }

    /**
     * 心跳配置
     */
    public static class Heartbeat {

        /**
         * 是否启用心跳检测
         */
        private boolean enabled = true;

        /**
         * 心跳间隔时间，单位：秒
         */
        private int interval = 30;

        /**
         * 心跳超时时间，单位：秒。超过此时间未收到客户端响应则关闭连接
         */
        private int timeout = 60;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getInterval() {
            return interval;
        }

        public void setInterval(int interval) {
            this.interval = interval;
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(int timeout) {
            this.timeout = timeout;
        }

    }

}
