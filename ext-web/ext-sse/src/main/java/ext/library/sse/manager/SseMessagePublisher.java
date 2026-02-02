package ext.library.sse.manager;

import ext.library.json.util.JsonUtil;
import ext.library.sse.domain.SseMessage;
import ext.library.sse.pubsub.PubSubService;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.ObjectUtil;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * SSE 消息发布器，负责跨服务器的消息发布与订阅
 */
public class SseMessagePublisher {

    private final String topic;
    private final PubSubService pubSubService;
    private final SseConnectionManager connectionManager;

    public SseMessagePublisher(SseConnectionManager connectionManager, PubSubService pubSubService, String topic) {
        this.connectionManager = connectionManager;
        this.pubSubService = pubSubService;
        this.topic = topic;
    }

    /**
     * 订阅 SSE 消息主题
     *
     * @param consumer 处理 SSE 消息的消费者函数
     */
    public void subscribe(Consumer<SseMessage> consumer) {
        pubSubService.subscribe(topic, SseMessage.class, consumer);
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
            pubSubService.publish(topic, JsonUtil.toJson(broadcastMessage));
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
        pubSubService.publish(topic, JsonUtil.toJson(broadcastMessage));
    }

}
