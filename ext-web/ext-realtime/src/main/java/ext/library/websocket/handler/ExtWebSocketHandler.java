package ext.library.websocket.handler;

import ext.library.security.domain.SecuritySession;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.trace.TraceContext;
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
import java.util.Map;
import java.util.Objects;

/**
 * WebSocket 消息处理器
 */
public class ExtWebSocketHandler extends AbstractWebSocketHandler {

    private static final String loginUserKey = "loginUser";

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
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(loginUserKey);
        if (Objects.isNull(loginUser)) {
            session.close(CloseStatus.BAD_DATA);
            Logs.info(EmojiSymbol.WEBSOCKET, "[连接] 无效的 token, sessionId: {}", session.getId());
            return;
        }
        String sessionKey = loginUser.getLoginId();
        connectionManager.addSession(sessionKey, session);
        heartbeatManager.recordActivity(session.getId());
        Logs.info(EmojiSymbol.WEBSOCKET, "[连接] sessionId: {}, userId: {}", session.getId(), sessionKey);
    }

    /**
     * 处理接收到的文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> traceSnapshot = bindTrace(session);
        try {
            SecuritySession loginUser = (SecuritySession) session.getAttributes().get(loginUserKey);
            String sessionKey = loginUser.getLoginId();
            // 记录活动时间
            heartbeatManager.recordActivity(session.getId());
            Logs.debug(EmojiSymbol.WEBSOCKET, "[消息] userId: {}, message: {}", sessionKey, message.getPayload());
        } finally {
            TraceContext.restore(traceSnapshot);
        }
    }

    /**
     * 处理接收到的二进制消息
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        Map<String, String> traceSnapshot = bindTrace(session);
        try {
            SecuritySession loginUser = (SecuritySession) session.getAttributes().get(loginUserKey);
            if (loginUser != null) {
                heartbeatManager.recordActivity(session.getId());
            }
            super.handleBinaryMessage(session, message);
        } finally {
            TraceContext.restore(traceSnapshot);
        }
    }

    /**
     * 消息处理不在 Servlet 线程上，MDC 里没有 traceId：用握手时存下的那个，
     * 让处理它的日志与出站请求跟连接同一条链路
     */
    private static Map<String, String> bindTrace(WebSocketSession session) {
        Object traceId = session.getAttributes().get(TraceContext.TRACE_ID_MDC_KEY);
        return TraceContext.bind(traceId instanceof String value && !value.isBlank() ? value : TraceContext.generate());
    }

    /**
     * 处理接收到的 Pong 消息（心跳响应）
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(loginUserKey);
        if (loginUser != null) {
            // 收到 Pong 表示客户端活跃
            heartbeatManager.recordActivity(session.getId());
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
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(loginUserKey);
        if (Objects.isNull(loginUser)) {
            Logs.info(EmojiSymbol.WEBSOCKET, "[断开] 无效的 token, sessionId: {}", session.getId());
            return;
        }
        String sessionKey = loginUser.getLoginId();
        connectionManager.removeSession(sessionKey, session.getId());
        heartbeatManager.removeActivity(session.getId());
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
