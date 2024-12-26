package ext.library.websocket.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.tool.$;
import ext.library.websocket.domain.WebSocketMessage;
import ext.library.websocket.holder.WebSocketSessionHolder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static ext.library.websocket.constant.WebSocketConstants.WEB_SOCKET_TOPIC;

/**
 * 工具类
 */
@Slf4j
@UtilityClass
public class WebSocketUtil {

    /**
     * 向指定的 WebSocket 会话发送消息
     *
     * @param sessionKey 要发送消息的用户 id
     * @param message    要发送的消息内容
     */
    public void sendMessage(String sessionKey, String message) {
        WebSocketSession session = WebSocketSessionHolder.getSessions(sessionKey);
        sendMessage(session, message);
    }

    /**
     * 订阅 WebSocket 消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理 WebSocket 消息的消费者函数
     */
    public void subscribeMessage(Consumer<WebSocketMessage> consumer) {
        RedisUtil.subscribe(WEB_SOCKET_TOPIC, WebSocketMessage.class, consumer);
    }

    /**
     * 发布 WebSocket 订阅消息
     *
     * @param webSocketMessage 要发布的 WebSocket 消息对象
     */
    public void publishMessage(@NotNull WebSocketMessage webSocketMessage) {
        List<String> unsentSessionKeys = new ArrayList<>();
        // 当前服务内 session，直接发送消息
        for (String sessionKey : webSocketMessage.getSessionKeys()) {
            if (WebSocketSessionHolder.existSession(sessionKey)) {
                WebSocketUtil.sendMessage(sessionKey, webSocketMessage.getMessage());
                continue;
            }
            unsentSessionKeys.add(sessionKey);
        }
        // 不在当前服务内 session，发布订阅消息
        if ($.isNotEmpty(unsentSessionKeys)) {
            WebSocketMessage broadcastMessage = new WebSocketMessage();
            broadcastMessage.setMessage(webSocketMessage.getMessage());
            broadcastMessage.setSessionKeys(unsentSessionKeys);
            log.info("WebSocket 发送主题订阅消息，topic:{},session keys:{},message:{}", WEB_SOCKET_TOPIC, unsentSessionKeys,
                    webSocketMessage.getMessage());
            RedisUtil.publish(WEB_SOCKET_TOPIC, JsonUtil.toJson(broadcastMessage));
        }
    }

    /**
     * 向所有的 WebSocket 会话发布订阅的消息 (群发)
     *
     * @param message 要发布的消息内容
     */
    public void publishAll(String message) {
        WebSocketMessage broadcastMessage = new WebSocketMessage();
        broadcastMessage.setMessage(message);
        log.info("WebSocket 发送主题订阅消息，topic:{},message:{}", WEB_SOCKET_TOPIC, message);
        RedisUtil.publish(WEB_SOCKET_TOPIC, JsonUtil.toJson(broadcastMessage));
    }

    /**
     * 向指定的 WebSocket 会话发送 Pong 消息
     *
     * @param session 要发送 Pong 消息的 WebSocket 会话
     */
    public void sendPongMessage(WebSocketSession session) {
        sendMessage(session, new PongMessage());
    }

    /**
     * 向指定的 WebSocket 会话发送文本消息
     *
     * @param session WebSocket 会话
     * @param message 要发送的文本消息内容
     */
    public void sendMessage(WebSocketSession session, String message) {
        sendMessage(session, new TextMessage(message));
    }

    /**
     * 向指定的 WebSocket 会话发送 WebSocket 消息对象
     *
     * @param session WebSocket 会话
     * @param message 要发送的 WebSocket 消息对象
     */
    private synchronized void sendMessage(WebSocketSession session,
                                          org.springframework.web.socket.WebSocketMessage<?> message) {
        if (session == null || !session.isOpen()) {
            log.warn("[send] session 会话已经关闭");
        } else {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.error("[send] session({}) 发送消息异常，message:{}", session, message, e);
            }
        }
    }

}
