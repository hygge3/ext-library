package ext.library.websocket.listener;

import ext.library.tool.$;
import ext.library.websocket.holder.WebSocketSessionHolder;
import ext.library.websocket.util.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

/**
 * WebSocket 主题订阅监听器
 */
@Slf4j
@Order(-1)
public class WebSocketTopicListener implements ApplicationRunner {

	/**
	 * 在 Spring Boot 应用程序启动时初始化 WebSocket 主题订阅监听器
	 * @param args 应用程序参数
	 * @throws Exception 初始化过程中可能抛出的异常
	 */
	@Override
	public void run(ApplicationArguments args) throws Exception {
		// 订阅 WebSocket 消息
		WebSocketUtil.subscribeMessage((message) -> {
			log.info("[⛓️] WebSocket 主题订阅收到消息，session keys:{},message:{}", message.getSessionKeys(), message.getMessage());
			// 如果 key 不为空就按照 key 发消息 如果为空就群发
			if ($.isNotEmpty(message.getSessionKeys())) {
				message.getSessionKeys().forEach(key -> {
					if (WebSocketSessionHolder.existSession(key)) {
						WebSocketUtil.sendMessage(key, message.getMessage());
					}
				});
			}
			else {
				WebSocketSessionHolder.getSessionsAll().forEach(key -> WebSocketUtil.sendMessage(key, message.getMessage()));
			}
		});
		log.info("[⛓️] 初始化 WebSocket 主题订阅监听器成功");
	}

}
