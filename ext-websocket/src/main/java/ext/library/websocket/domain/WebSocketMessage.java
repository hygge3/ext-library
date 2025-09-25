package ext.library.websocket.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息的 dto
 */
public class WebSocketMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的 session key 列表
     */
    private List<String> sessionKeys;

    /**
     * 需要发送的消息
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
}