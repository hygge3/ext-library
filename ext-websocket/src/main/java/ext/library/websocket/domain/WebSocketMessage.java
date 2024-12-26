package ext.library.websocket.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 消息的 dto
 *
 * @author zendwang
 */
@Data
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

}
