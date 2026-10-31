package ext.library.websocket.manager;

import ext.library.core.pubsub.PubSub;
import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;
import ext.library.websocket.domain.WebSocketMessage;

import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * WebSocket 消息发布器，负责跨服务器的消息发布与订阅
 * <p>
 * 与 SSE 侧同一形状：跨实例广播走 ext-core 的 {@link PubSub}，容器里没有它时退化为<b>单实例</b>模式
 * （只在当前 JVM 内投递），多实例部署必须引入 ext-redis 或 ext-postgres。
 */
public class WebSocketMessagePublisher {

    private final String topic;
    private final PubSub pubSub;
    private final WebSocketConnectionManager connectionManager;

    /**
     * @param connectionManager 连接管理器
     * @param pubSub            跨实例广播用的发布订阅实现，{@code null} 表示单实例模式
     * @param topic             广播主题
     */
    public WebSocketMessagePublisher(WebSocketConnectionManager connectionManager, @Nullable PubSub pubSub, String topic) {
        this.connectionManager = connectionManager;
        this.pubSub = pubSub;
        this.topic = topic;
    }

    /**
     * 订阅 WebSocket 消息主题
     * <p>
     * 单实例模式（没有 {@code PubSub}）下直接返回：没有别的实例会往主题里发消息。
     *
     * @param consumer 处理消息的消费者函数
     */
    public void subscribe(Consumer<WebSocketMessage> consumer) {
        if (pubSub == null) {
            Logs.info(EmojiSymbol.WEBSOCKET, "未引入 ext-redis / ext-postgres，WebSocket 以单实例模式运行，跳过主题订阅：topic:{}", topic);
            return;
        }
        pubSub.subscribe(topic, WebSocketMessage.class, consumer);
    }

    /**
     * 把广播消息交给发布订阅（单实例模式下丢弃，消息已在本机投递完）
     */
    private void publishRemote(String payload) {
        if (pubSub == null) {
            return;
        }
        pubSub.publish(topic, payload);
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
            publishRemote(JsonUtil.toJson(broadcastMessage));
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
        publishRemote(JsonUtil.toJson(broadcastMessage));
    }

}
