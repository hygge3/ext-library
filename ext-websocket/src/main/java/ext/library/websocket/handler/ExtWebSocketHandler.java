package ext.library.websocket.handler;

import ext.library.core.util.SpringUtil;
import ext.library.security.domain.SecuritySession;
import ext.library.tool.holder.Lazy;
import ext.library.websocket.config.properties.WebSocketProperties;
import ext.library.websocket.domain.WebSocketMessage;
import ext.library.websocket.holder.WebSocketSessionHolder;
import ext.library.websocket.util.WebSocketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static ext.library.websocket.constant.WebSocketConstants.LOGIN_USER_KEY;

/**
 * WebSocketHandler 实现类
 */
public class ExtWebSocketHandler extends AbstractWebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Lazy<WebSocketProperties> properties = Lazy.of(() -> SpringUtil.getBean(WebSocketProperties.class));

    /**
     * 连接成功后
     */
    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) throws IOException {
        // 实现 session 支持并发，可参考 https://blog.csdn.net/abu935009066/article/details/131218149
        session = new ConcurrentWebSocketSessionDecorator(session,
                properties.get().getSendTimeLimit(),
                properties.get().getBufferSizeLimit());
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        if (Objects.isNull(loginUser)) {
            session.close(CloseStatus.BAD_DATA);
            log.info("[⛓️][connect] 无效的 token. sessionId: {}", session.getId());
            return;
        }
        WebSocketSessionHolder.addSession(loginUser.getLoginId(), session);
        log.info("[⛓️][connect] sessionId: {},userId:{}", session.getId(), loginUser.getLoginId());
    }

    /**
     * 处理接收到的文本消息
     *
     * @param session WebSocket 会话
     * @param message 接收到的文本消息
     *
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleTextMessage(@Nonnull WebSocketSession session, @Nonnull TextMessage message) throws Exception {
        // 从 WebSocket 会话中获取登录用户信息
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);

        // 创建 WebSocket 消息 DTO 对象
        WebSocketMessage webSocketMessage = new WebSocketMessage();
        webSocketMessage.setSessionKeys(List.of(loginUser.getLoginId()));
        webSocketMessage.setMessage(message.getPayload());
        WebSocketUtil.publishMessage(webSocketMessage);
    }

    /**
     * 处理接收到的二进制消息
     *
     * @param session WebSocket 会话
     * @param message 接收到的二进制消息
     *
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleBinaryMessage(@Nonnull WebSocketSession session, @Nonnull BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的 Pong 消息（心跳监测）
     *
     * @param session WebSocket 会话
     * @param message 接收到的 Pong 消息
     *
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handlePongMessage(@Nonnull WebSocketSession session, @Nonnull PongMessage message) throws Exception {
        WebSocketUtil.sendPongMessage(session);
    }

    /**
     * 处理 WebSocket 传输错误
     *
     * @param session   WebSocket 会话
     * @param exception 发生的异常
     *
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleTransportError(@Nonnull WebSocketSession session, @Nonnull Throwable exception) throws Exception {
        log.error("[⛓️][transport error] sessionId: {} , exception:{}", session.getId(), exception.getMessage());
    }

    /**
     * 在 WebSocket 连接关闭后执行清理操作
     *
     * @param session WebSocket 会话
     * @param status  关闭状态信息
     */
    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, @Nonnull CloseStatus status) {
        SecuritySession loginUser = (SecuritySession) session.getAttributes().get(LOGIN_USER_KEY);
        if (Objects.isNull(loginUser)) {
            log.info("[⛓️][disconnect] 无效的 token. sessionId: {}", session.getId());
            return;
        }
        WebSocketSessionHolder.removeSession(loginUser.getLoginId());
        log.info("[⛓️][disconnect] sessionId: {},userId:{}", session.getId(), loginUser.getLoginId());
    }

    /**
     * 指示处理程序是否支持接收部分消息
     *
     * @return 如果支持接收部分消息，则返回 true；否则返回 false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}