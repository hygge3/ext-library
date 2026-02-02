package ext.library.websocket.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * WebSocket 消息传输对象，用于跨服务器消息推送
 */
public class WebSocketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的会话键列表（用户 ID），为空时表示群发
     */
    private List<String> sessionKeys;

    /**
     * 需要发送的消息内容
     */
    private String message;

    public List<String> getSessionKeys() {
        return sessionKeys;
    }

    public void setSessionKeys(List<String> sessionKeys) {
        this.sessionKeys = sessionKeys;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof WebSocketMessage;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $sessionKeys = this.getSessionKeys();
        result = result * PRIME + ($sessionKeys == null ? 43 : $sessionKeys.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof WebSocketMessage other)) return false;
        if (!other.canEqual(this)) return false;
        final Object this$sessionKeys = this.getSessionKeys();
        final Object other$sessionKeys = other.getSessionKeys();
        if (!Objects.equals(this$sessionKeys, other$sessionKeys)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        return Objects.equals(this$message, other$message);
    }

    @Override
    public String toString() {
        return "WebSocketMessage{" +
                "sessionKeys=" + sessionKeys +
                ", message='" + message + '\'' +
                '}';
    }

}
