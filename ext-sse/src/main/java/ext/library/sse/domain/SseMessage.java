package ext.library.sse.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * 消息的 dto
 */
public class SseMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的 session key 列表
     */
    private List<String> userIds;

    /**
     * 需要发送的消息
     */
    private String message;

    public SseMessage() {}

    public List<String> getUserIds() {return this.userIds;}

    public void setUserIds(List<String> userIds) {this.userIds = userIds;}

    public String getMessage() {return this.message;}

    public void setMessage(String message) {this.message = message;}

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

    protected boolean canEqual(final Object other) {return other instanceof SseMessage;}

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $userIds = this.getUserIds();
        result = result * PRIME + ($userIds == null ? 43 : $userIds.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    public String toString() {return "SseMessage(userIds=" + this.getUserIds() + ", message=" + this.getMessage() + ")";}
}