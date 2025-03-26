package ext.library.sse.util;

import ext.library.core.util.SpringUtil;
import ext.library.sse.domain.SseMessage;
import ext.library.sse.manager.SseEmitterManager;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 工具类
 */
@Slf4j
@UtilityClass
public class SseUtil {

	 final static SseEmitterManager MANAGER = SpringUtil.getBean(SseEmitterManager.class);

	/**
	 * 向指定的 WebSocket 会话发送消息
	 * @param userId 要发送消息的用户 id
	 * @param message 要发送的消息内容
	 */
	public  void sendMessage(String userId, String message) {
		MANAGER.sendMessage(userId, message);
	}

	/**
	 * 本机全用户会话发送消息
	 * @param message 要发送的消息内容
	 */
	public  void sendMessage(String message) {
		MANAGER.sendMessage(message);
	}

	/**
	 * 发布 SSE 订阅消息
	 * @param sseMessage 要发布的 SSE 消息对象
	 */
	public  void publishMessage(SseMessage sseMessage) {
		MANAGER.publishMessage(sseMessage);
	}

	/**
	 * 向所有的用户发布订阅的消息 (群发)
	 * @param message 要发布的消息内容
	 */
	public  void publishAll(String message) {
		MANAGER.publishAll(message);
	}

}
