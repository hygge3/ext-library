package ext.library.websocket.util;

import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.tool.core.VirtualThreadPools;
import ext.library.tool.util.ObjectUtil;
import ext.library.websocket.domain.WebSocketMessage;
import ext.library.websocket.holder.WebSocketSessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.SessionLimitExceededException;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static ext.library.websocket.constant.WebSocketConstants.WEB_SOCKET_TOPIC;

/**
 * 工具类
 */
public class WebSocketUtil {
    private static final Logger log = LoggerFactory.getLogger(WebSocketUtil.class);

    /**
     * 向指定的 WebSocket 会话发送消息
     *
     * @param sessionKey 要发送消息的用户 id
     * @param message    要发送的消息内容
     */
    public static void sendMessage(String sessionKey, String message) {
        WebSocketSession session = WebSocketSessionHolder.getSessions(sessionKey);
        sendMessage(session, message);
    }

    /**
     * 订阅 WebSocket 消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理 WebSocket 消息的消费者函数
     */
    public static void subscribeMessage(Consumer<WebSocketMessage> consumer) {
        RedisUtil.subscribe(WEB_SOCKET_TOPIC, WebSocketMessage.class, consumer);
    }

    /**
     * 发布 WebSocket 订阅消息
     *
     * @param webSocketMessage 要发布的 WebSocket 消息对象
     */
    public static void publishMessage(@Nonnull WebSocketMessage webSocketMessage) {
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
        if (ObjectUtil.isNotEmpty(unsentSessionKeys)) {
            WebSocketMessage broadcastMessage = new WebSocketMessage();
            broadcastMessage.setMessage(webSocketMessage.getMessage());
            broadcastMessage.setSessionKeys(unsentSessionKeys);
            log.info("[⛓️] WebSocket 发送主题订阅消息，topic:{},session keys:{},message:{}", WEB_SOCKET_TOPIC, unsentSessionKeys, webSocketMessage.getMessage());
            RedisUtil.publish(WEB_SOCKET_TOPIC, JsonUtil.toJson(broadcastMessage));
        }
    }

    /**
     * 向所有的 WebSocket 会话发布订阅的消息 (群发)
     *
     * @param message 要发布的消息内容
     */
    public static void publishAll(String message) {
        WebSocketMessage broadcastMessage = new WebSocketMessage();
        broadcastMessage.setMessage(message);
        log.info("[⛓️] WebSocket 发送主题订阅消息，topic:{},message:{}", WEB_SOCKET_TOPIC, message);
        RedisUtil.publish(WEB_SOCKET_TOPIC, JsonUtil.toJson(broadcastMessage));
    }

    /**
     * 向指定的 WebSocket 会话发送 Pong 消息
     *
     * @param session 要发送 Pong 消息的 WebSocket 会话
     */
    public static void sendPongMessage(WebSocketSession session) {
        sendMessage(session, new PongMessage());
    }

    /**
     * 向指定的 WebSocket 会话发送文本消息
     *
     * @param session WebSocket 会话
     * @param message 要发送的文本消息内容
     */
    public static void sendMessage(WebSocketSession session, String message) {
        sendMessage(session, new TextMessage(message));
    }

    /**
     * 向指定的 WebSocket 会话发送 WebSocket 消息对象
     *
     * @param session WebSocket 会话
     * @param message 要发送的 WebSocket 消息对象
     */
    private static synchronized void sendMessage(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) {
        VirtualThreadPools.execute("WebSocket Send", () -> {
            if (session == null || !session.isOpen()) {
                log.warn("[⛓️][send] session 会话已经关闭");
            } else {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    log.error("[⛓️][send] session({}) 发送消息异常，message:{}", session, message, e);
                } catch (SessionLimitExceededException ex) {
                    // 一旦有一条消息发送超时，或者发送数据大于限制，limitExceeded 标志位就会被设置成 true，标志这这个 session 被关闭
                    // 后面的发送调用都是直接返回不处理，但只是被标记为关闭连接本身可能实际上并没有关闭，这是一个坑需要注意。
                    try {
                        session.close();
                    } catch (IOException e) {
                        log.error("[⛓️][close] 主动关闭 session ({}) 连接失败", session.getId());
                    }
                    log.error("[⛓️][error] session ({}) 发送消息失败", session.getId());
                }
            }
        });
    }
}