package ext.library.websocket.handler;

import ext.library.security.domain.SecuritySession;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.websocket.manager.WebSocketConnectionManager;
import ext.library.websocket.manager.WebSocketHeartbeatManager;
import ext.library.websocket.properties.WebSocketProperties;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.Objects;

/**
 * WebSocket 消息处理器
 */
public class ExtWebSocketHandler extends AbstractWebSocketHandler {

    private static final String LOGIN_USER_KEY = "loginUser";

    private final WebSocketConnectionManager connectionManager;
    private final WebSocketHeartbeatManager heartbeatManager;
    private final WebSocketProperties properties;

    public ExtWebSocketHandler(WebSocketConnectionManager connectionManager, WebSocketHeartbeatManager heartbeatManager, WebSocketProperties properties) {
        this.connectionManager = connectionManager;
        this.heartbeatManager = heartbeatManager;
        this.properties = properties;
    }

    /**
     * 连接建立成功后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // 包装 session 支持并发发送
        session = new ConcurrentWebSocketSessionDecorator(session, properties.getSendTimeLimit(), properties.getBufferSizeLimit());
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        if (Objects.isNull(loginUser)) {
            session.close(CloseStatus.BAD_DATA);
            Logs.info(EmojiSymbol.WEBSOCKET, "[连接] 无效的 token, sessionId: {}", session.getId());
            return;
        }
        String sessionKey = loginUser.getLoginId();
        connectionManager.addSession(sessionKey, session);
        heartbeatManager.recordActivity(sessionKey);
        Logs.info(EmojiSymbol.WEBSOCKET, "[连接] sessionId: {}, userId: {}", session.getId(), sessionKey);
    }

    /**
     * 处理接收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        String sessionKey = loginUser.getLoginId();
        // 记录活动时间
        heartbeatManager.recordActivity(sessionKey);
        Logs.debug(EmojiSymbol.WEBSOCKET, "[消息] userId: {}, message: {}", sessionKey, message.getPayload());
    }

    /**
     * 处理接收到的二进制消息
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        if (loginUser != null) {
            heartbeatManager.recordActivity(loginUser.getLoginId());
        }
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的 Pong 消息（心跳响应）
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        if (loginUser != null) {
            // 收到 Pong 表示客户端活跃
            heartbeatManager.recordActivity(loginUser.getLoginId());
            Logs.debug(EmojiSymbol.WEBSOCKET, "[心跳] 收到 Pong, userId: {}", loginUser.getLoginId());
        }
    }

    /**
     * 处理传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Logs.warn(EmojiSymbol.WEBSOCKET, "[传输错误] sessionId: {}, error: {}", session.getId(), exception.getMessage());
    }

    /**
     * 连接关闭后
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        if (Objects.isNull(loginUser)) {
            Logs.info(EmojiSymbol.WEBSOCKET, "[断开] 无效的 token, sessionId: {}", session.getId());
            return;
        }
        String sessionKey = loginUser.getLoginId();
        connectionManager.removeSession(sessionKey);
        heartbeatManager.removeActivity(sessionKey);
        Logs.info(EmojiSymbol.WEBSOCKET, "[断开] sessionId: {}, userId: {}", session.getId(), sessionKey);
    }

    /**
     * 是否支持部分消息
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
