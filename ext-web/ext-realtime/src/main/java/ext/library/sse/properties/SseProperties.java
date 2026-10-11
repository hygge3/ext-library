package ext.library.sse.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

/**
 * SSE 配置项
 */
@ConfigurationProperties(SseProperties.PREFIX)
@Validated
public class SseProperties {

    public static final String PREFIX = "ext.sse";

    /**
     * 默认发布订阅主题
     */
    public static final String DEFAULT_TOPIC = "ext:sse";

    /**
     * 启用 SSE 功能
     */
    private Boolean enabled;

    /**
     * SSE 端点路径
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    private String path;

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

    }

}
