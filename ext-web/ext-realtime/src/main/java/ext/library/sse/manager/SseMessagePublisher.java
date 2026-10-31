package ext.library.sse.manager;

import ext.library.core.pubsub.PubSub;
import ext.library.json.util.JsonUtil;
import ext.library.sse.domain.SseMessage;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;

import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * SSE 消息发布器，负责跨服务器的消息发布与订阅
 * <p>
 * 跨实例广播走 ext-core 的 {@link PubSub}（由 ext-redis 的 {@code RedisPubSub} 或 ext-postgres 的
 * {@code PostgresPubSub} 提供，两个后端都由各自模块自动装配）。容器里没有 {@code PubSub} 时退化为
 * <b>单实例</b>模式：消息只在当前 JVM 内投递，多实例部署必须引入其中一个后端。
 */
public class SseMessagePublisher {

    private final String topic;
    private final PubSub pubSub;
    private final SseConnectionManager connectionManager;

    /**
     * @param connectionManager 连接管理器
     * @param pubSub            跨实例广播用的发布订阅实现，{@code null} 表示单实例模式
     * @param topic             广播主题
     */
    public SseMessagePublisher(SseConnectionManager connectionManager, @Nullable PubSub pubSub, String topic) {
        this.connectionManager = connectionManager;
        this.pubSub = pubSub;
        this.topic = topic;
    }

    /**
     * 订阅 SSE 消息主题
     * <p>
     * 单实例模式（没有 {@code PubSub}）下直接返回：没有别的实例会往主题里发消息。
     *
     * @param consumer 处理 SSE 消息的消费者函数
     */
    public void subscribe(Consumer<SseMessage> consumer) {
        if (pubSub == null) {
            Logs.info(EmojiSymbol.SSE, "未引入 ext-redis / ext-postgres，SSE 以单实例模式运行，跳过主题订阅：topic:{}", topic);
            return;
        }
        pubSub.subscribe(topic, SseMessage.class, consumer);
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
     * 发布 SSE 消息到指定用户
     * <p>
     * 优先向本地连接发送，不在本地的用户通过发布订阅转发
     *
     * @param sseMessage 要发布的 SSE 消息对象
     */
    public void publish(@Nonnull SseMessage sseMessage) {
        List<String> unsentUserIds = new ArrayList<>();

        // 当前服务内用户，直接发送消息
        for (String userId : sseMessage.getUserIds()) {
            if (connectionManager.hasConnection(userId)) {
                connectionManager.sendMessage(userId, sseMessage.getMessage());
            } else {
                unsentUserIds.add(userId);
            }
        }

        // 不在当前服务内的用户，发布订阅消息
        if (ObjectUtil.isNotEmpty(unsentUserIds)) {
            SseMessage broadcastMessage = new SseMessage();
            broadcastMessage.setMessage(sseMessage.getMessage());
            broadcastMessage.setUserIds(unsentUserIds);
            Logs.info(EmojiSymbol.SSE, "SSE 发布订阅消息，topic:{}, userIds:{}, message:{}", topic, unsentUserIds, sseMessage.getMessage());
            publishRemote(JsonUtil.toJson(broadcastMessage));
        }
    }

    /**
     * 向所有用户发布消息（群发）
     *
     * @param message 要发布的消息内容
     */
    public void publishToAll(String message) {
        SseMessage broadcastMessage = new SseMessage();
        broadcastMessage.setMessage(message);
        Logs.info(EmojiSymbol.SSE, "SSE 发布群发消息，topic:{}, message:{}", topic, message);
        publishRemote(JsonUtil.toJson(broadcastMessage));
    }

}
