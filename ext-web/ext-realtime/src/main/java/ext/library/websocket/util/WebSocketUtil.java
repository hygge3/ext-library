package ext.library.websocket.util;

import ext.library.core.util.SpringUtil;
import ext.library.tool.holder.Lazy;
import ext.library.websocket.domain.WebSocketMessage;
import ext.library.websocket.manager.WebSocketConnectionManager;
import ext.library.websocket.manager.WebSocketMessagePublisher;

/**
 * WebSocket 工具类，提供消息发送和发布订阅功能
 */
public final class WebSocketUtil {

    private static final Lazy<WebSocketConnectionManager> CONNECTION_MANAGER = Lazy.of(() -> SpringUtil.getBean(WebSocketConnectionManager.class));
    private static final Lazy<WebSocketMessagePublisher> MESSAGE_PUBLISHER = Lazy.of(() -> SpringUtil.getBean(WebSocketMessagePublisher.class));

    private WebSocketUtil() {
    }

    /**
     * 向指定会话发送消息（仅本机）
     *
     * @param sessionKey 会话键（用户 ID）
     * @param message    消息内容
     */
    public static void sendMessage(String sessionKey, String message) {
        CONNECTION_MANAGER.get().sendMessage(sessionKey, message);
    }

    /**
     * 向本机所有会话发送消息
     *
     * @param message 消息内容
     */
    public static void sendMessage(String message) {
        CONNECTION_MANAGER.get().sendMessageToAll(message);
    }

    /**
     * 发布 WebSocket 消息到指定用户（支持跨服务器）
     *
     * @param webSocketMessage 消息对象
     */
    public static void publishMessage(WebSocketMessage webSocketMessage) {
        MESSAGE_PUBLISHER.get().publish(webSocketMessage);
    }

    /**
     * 向所有用户发布消息（群发，支持跨服务器）
     *
     * @param message 消息内容
     */
    public static void publishAll(String message) {
        MESSAGE_PUBLISHER.get().publishToAll(message);
    }

}
