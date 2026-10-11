package ext.library.sse.manager;

import ext.library.sse.domain.SseMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * SSE 管理器门面，统一对外提供 SSE 连接管理和消息发布功能
 *
 * @see SseConnectionManager 连接管理
 * @see SseMessagePublisher 消息发布
 */
public class SseEmitterManager {

    private final SseConnectionManager connectionManager;
    private final SseMessagePublisher messagePublisher;

    public SseEmitterManager(SseConnectionManager connectionManager, SseMessagePublisher messagePublisher) {
        this.connectionManager = connectionManager;
        this.messagePublisher = messagePublisher;
    }

    /**
     * 建立与指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符
     * @param token  用户的唯一令牌
     * @return SseEmitter 实例
     */
    public SseEmitter connect(String userId, String token) {
        return connectionManager.connect(userId, token);
    }

    /**
     * 断开指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符
     * @param token  用户的唯一令牌
     */
    public void disconnect(String userId, String token) {
        connectionManager.disconnect(userId, token);
    }

    /**
     * 订阅 SSE 消息主题
     *
     * @param consumer 处理 SSE 消息的消费者函数
     */
    public void subscribeMessage(Consumer<SseMessage> consumer) {
        messagePublisher.subscribe(consumer);
    }

    /**
     * 向指定用户的 SSE 会话发送消息（仅本机）
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    public void sendMessage(String userId, String message) {
        connectionManager.sendMessage(userId, message);
    }

    /**
     * 向本机所有用户会话发送消息
     *
     * @param message 消息内容
     */
    public void sendMessage(String message) {
        connectionManager.sendMessageToAll(message);
    }

    /**
     * 发布 SSE 消息到指定用户（支持跨服务器）
     *
     * @param sseMessage SSE 消息对象
     */
    public void publishMessage(@Nonnull SseMessage sseMessage) {
        messagePublisher.publish(sseMessage);
    }

    /**
     * 向所有用户发布消息（群发，支持跨服务器）
     *
     * @param message 消息内容
     */
    public void publishAll(String message) {
        messagePublisher.publishToAll(message);
    }

}
