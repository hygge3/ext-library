package ext.library.websocket.listener;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.websocket.manager.WebSocketConnectionManager;
import ext.library.websocket.manager.WebSocketMessagePublisher;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

import jakarta.annotation.Resource;

/**
 * WebSocket 主题订阅监听器
 */
@Order(-1)
public class WebSocketTopicListener implements ApplicationRunner {

    @Resource
    private WebSocketConnectionManager connectionManager;

    @Resource
    private WebSocketMessagePublisher messagePublisher;

    /**
     * 在应用程序启动时初始化 WebSocket 主题订阅监听器
     *
     * @param args 应用程序参数
     */
    @Override
    public void run(@NonNull ApplicationArguments args) {
        messagePublisher.subscribe(message -> {
            Logs.info(EmojiSymbol.WEBSOCKET, "WebSocket 收到订阅消息，sessionKeys:{}, message:{}", message.getSessionKeys(), message.getMessage());
            // 如果 sessionKeys 不为空就按会话发消息，否则群发
            if (ObjectUtil.isNotEmpty(message.getSessionKeys())) {
                message.getSessionKeys().forEach(key -> {
                    if (connectionManager.hasSession(key)) {
                        connectionManager.sendMessage(key, message.getMessage());
                    }
                });
            } else {
                connectionManager.sendMessageToAll(message.getMessage());
            }
        });
        Logs.info(EmojiSymbol.WEBSOCKET, "初始化 WebSocket 主题订阅监听器成功");
    }

}
