package ext.library.sse.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 消息的 dto
 */
@Data
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

}
