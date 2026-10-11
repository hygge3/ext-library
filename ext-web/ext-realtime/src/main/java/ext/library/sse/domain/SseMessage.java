package ext.library.sse.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * SSE 消息传输对象，用于跨服务器消息推送
 */
public class SseMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的用户 ID 列表，为空时表示群发
     */
    private List<String> userIds;

    /**
     * 需要发送的消息内容
     */
    private String message;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof SseMessage;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userIds = this.getUserIds();
        result = result * PRIME + ($userIds == null ? 43 : $userIds.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof SseMessage other)) return false;
        if (!other.canEqual(this)) return false;
        final Object this$userIds = this.getUserIds();
        final Object other$userIds = other.getUserIds();
        if (!Objects.equals(this$userIds, other$userIds)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        return Objects.equals(this$message, other$message);
    }

    @Override
    public String toString() {
        return "SseMessage{" +
                "userIds=" + userIds +
                ", message='" + message + '\'' +
                '}';
    }

}
