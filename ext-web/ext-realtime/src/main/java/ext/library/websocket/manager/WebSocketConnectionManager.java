package ext.library.websocket.manager;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.runtime.VirtualThreadPools;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.SessionLimitExceededException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 连接管理器，负责会话的存储、检索和消息发送
 */
public class WebSocketConnectionManager {

    /**
     * sessionKey（用户 ID）→ (sessionId → session)
     * <p>
     * 同一用户允许多个标签页同时在线，按 sessionId 存，关掉其中一个标签页不会把其他连接一起删掉。
     */
    private static final Map<String, Map<String, WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();

    /**
     * 添加 WebSocket 会话
     *
     * @param sessionKey 会话键（通常为用户 ID）
     * @param session    WebSocket 会话
     */
    public void addSession(String sessionKey, WebSocketSession session) {
        userSessionMap.computeIfAbsent(sessionKey, _ -> new ConcurrentHashMap<>()).put(session.getId(), session);
    }

    /**
     * 移除单个 WebSocket 会话
     *
     * @param sessionKey 会话键
     * @param sessionId  会话 ID
     */
    public void removeSession(String sessionKey, String sessionId) {
        userSessionMap.computeIfPresent(sessionKey, (_, sessions) -> {
            sessions.remove(sessionId);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    /**
     * 获取指定会话键的所有 WebSocket 会话
     *
     * @param sessionKey 会话键
     * @return WebSocket 会话列表，不存在时返回空列表
     */
    public List<WebSocketSession> getSessions(String sessionKey) {
        Map<String, WebSocketSession> sessions = userSessionMap.get(sessionKey);
        return sessions == null ? List.of() : List.copyOf(sessions.values());
    }

    /**
     * 获取所有会话键
     *
     * @return 所有会话键集合
     */
    public Set<String> getAllSessionKeys() {
        return userSessionMap.keySet();
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionKey 会话键
     * @return 是否存在
     */
    public boolean hasSession(String sessionKey) {
        return userSessionMap.containsKey(sessionKey);
    }

    /**
     * 向指定会话发送文本消息
     *
     * @param sessionKey 会话键
     * @param message    消息内容
     */
    public void sendMessage(String sessionKey, String message) {
        getSessions(sessionKey).forEach(session -> sendMessage(session, new TextMessage(message)));
    }

    /**
     * 向所有会话发送文本消息
     *
     * @param message 消息内容
     */
    public void sendMessageToAll(String message) {
        for (String sessionKey : userSessionMap.keySet()) {
            sendMessage(sessionKey, message);
        }
    }

    /**
     * 发送 WebSocket 消息
     *
     * @param session WebSocket 会话
     * @param message 消息对象
     */
    private void sendMessage(WebSocketSession session, WebSocketMessage<?> message) {
        VirtualThreadPools.execute("WebSocket Send", () -> {
            if (session == null || !session.isOpen()) {
                Logs.warn(EmojiSymbol.WEBSOCKET, "[发送] session 会话已关闭或不存在");
                return;
            }
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                Logs.warn(EmojiSymbol.WEBSOCKET, "[发送] session({}) 发送消息异常: {}", session.getId(), e.getMessage());
            } catch (SessionLimitExceededException e) {
                // 发送超时或数据超限，需要主动关闭连接
                Logs.warn(EmojiSymbol.WEBSOCKET, "[发送] session({}) 超出限制，关闭连接: {}", session.getId(), e.getMessage());
                try {
                    session.close();
                } catch (IOException ex) {
                    Logs.warn(EmojiSymbol.WEBSOCKET, "[关闭] session({}) 关闭失败: {}", session.getId(), ex.getMessage());
                }
            }
        });
    }

}
