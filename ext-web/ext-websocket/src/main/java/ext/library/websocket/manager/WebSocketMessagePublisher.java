package ext.library.websocket.manager;

import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.websocket.domain.WebSocketMessage;
import ext.library.websocket.pubsub.PubSubService;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * WebSocket 消息发布器，负责跨服务器的消息发布与订阅
 */
public class WebSocketMessagePublisher {

    private final String topic;
    private final PubSubService pubSubService;
    private final WebSocketConnectionManager connectionManager;

    public WebSocketMessagePublisher(WebSocketConnectionManager connectionManager, PubSubService pubSubService, String topic) {
        this.connectionManager = connectionManager;
        this.pubSubService = pubSubService;
        this.topic = topic;
    }

    /**
     * 订阅 WebSocket 消息主题
     *
     * @param consumer 处理消息的消费者函数
     */
    public void subscribe(Consumer<WebSocketMessage> consumer) {
        pubSubService.subscribe(topic, WebSocketMessage.class, consumer);
    }

    /**
     * 发布 WebSocket 消息到指定用户
     * <p>
     * 优先向本地连接发送，不在本地的用户通过发布订阅转发
     *
     * @param webSocketMessage 要发布的消息对象
     */
    public void publish(@Nonnull WebSocketMessage webSocketMessage) {
        List<String> unsentSessionKeys = new ArrayList<>();

        // 当前服务内会话，直接发送消息
        for (String sessionKey : webSocketMessage.getSessionKeys()) {
            if (connectionManager.hasSession(sessionKey)) {
                connectionManager.sendMessage(sessionKey, webSocketMessage.getMessage());
            } else {
                unsentSessionKeys.add(sessionKey);
            }
        }

        // 不在当前服务内的会话，发布订阅消息
        if (ObjectUtil.isNotEmpty(unsentSessionKeys)) {
            WebSocketMessage broadcastMessage = new WebSocketMessage();
            broadcastMessage.setMessage(webSocketMessage.getMessage());
            broadcastMessage.setSessionKeys(unsentSessionKeys);
            Logs.info(EmojiSymbol.WEBSOCKET, "WebSocket 发布订阅消息，topic:{}, sessionKeys:{}, message:{}", topic, unsentSessionKeys, webSocketMessage.getMessage());
            pubSubService.publish(topic, JsonUtil.toJson(broadcastMessage));
        }
    }

    /**
     * 向所有用户发布消息（群发）
     *
     * @param message 消息内容
     */
    public void publishToAll(String message) {
        WebSocketMessage broadcastMessage = new WebSocketMessage();
        broadcastMessage.setMessage(message);
        Logs.info(EmojiSymbol.WEBSOCKET, "WebSocket 发布群发消息，topic:{}, message:{}", topic, message);
        pubSubService.publish(topic, JsonUtil.toJson(broadcastMessage));
    }

}
