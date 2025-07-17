package ext.library.sse.manager;

import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import ext.library.sse.domain.SseMessage;
import ext.library.tool.$;
import ext.library.tool.core.ThreadPools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 管理 Server-Sent Events (SSE) 连接
 */
@Slf4j
public class SseEmitterManager {

    /**
     * 订阅的频道
     */
    final static String SSE_TOPIC = "global:sse";

    final static Map<String, Map<String, SseEmitter>> USER_TOKEN_EMITTERS = new ConcurrentHashMap<>();

    /**
     * 建立与指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     *
     * @return 返回一个 SseEmitter 实例，客户端可以通过该实例接收 SSE 事件
     */
    public SseEmitter connect(String userId, String token) {
        // 从 USER_TOKEN_EMITTERS 中获取或创建当前用户的 SseEmitter 映射表（ConcurrentHashMap）
        // 每个用户可以有多个 SSE 连接，通过 token 进行区分
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        // 创建一个新的 SseEmitter 实例，超时时间设置为 0 表示无限制
        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(token, emitter);

        // 当 emitter 完成、超时或发生错误时，从映射表中移除对应的 token
        emitter.onCompletion(() -> emitters.remove(token));
        emitter.onTimeout(() -> emitters.remove(token));
        emitter.onError((e) -> emitters.remove(token));

        try {
            // 向客户端发送一条连接成功的事件
            emitter.send(SseEmitter.event().comment("connected").data("connected"));
        } catch (IOException e) {
            // 如果发送消息失败，则从映射表中移除 emitter
            emitters.remove(token);
        }
        return emitter;
    }

    /**
     * 断开指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     */
    public void disconnect(String userId, String token) {
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (emitters != null) {
            try {
                SseEmitter emitter = emitters.get(token);
                emitter.send(SseEmitter.event().comment("disconnected").data("disconnected"));
                emitter.complete();
            } catch (Exception ignore) {
                emitters.remove(token);
            }
        }
    }

    /**
     * 订阅 SSE 消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理 SSE 消息的消费者函数
     */
    public void subscribeMessage(Consumer<SseMessage> consumer) {
        RedisUtil.subscribe(SSE_TOPIC, SseMessage.class, consumer);
    }

    /**
     * 向指定的用户会话发送消息
     *
     * @param userId  要发送消息的用户 id
     * @param message 要发送的消息内容
     */
    public void sendMessage(String userId, String message) {
        ThreadPools.execute(() -> {
            Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
            if (emitters != null) {
                for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                    try {
                        entry.getValue().send(SseEmitter.event().name("message").data(message));
                    } catch (Exception e) {
                        emitters.remove(entry.getKey());
                    }
                }
            }
        });
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param message 要发送的消息内容
     */
    public void sendMessage(String message) {
        for (String userId : USER_TOKEN_EMITTERS.keySet()) {
            sendMessage(userId, message);
        }
    }

    /**
     * 发布 SSE 订阅消息
     *
     * @param sseMessage 要发布的 SSE 消息对象
     */
    public void publishMessage(@Nonnull SseMessage sseMessage) {
        List<String> unsentUserIds = new ArrayList<>();
        // 当前服务内用户，直接发送消息
        for (String userId : sseMessage.getUserIds()) {
            if (USER_TOKEN_EMITTERS.containsKey(userId)) {
                sendMessage(userId, sseMessage.getMessage());
                continue;
            }
            unsentUserIds.add(userId);
        }
        // 不在当前服务内用户，发布订阅消息
        if ($.isNotEmpty(unsentUserIds)) {
            SseMessage broadcastMessage = new SseMessage();
            broadcastMessage.setMessage(sseMessage.getMessage());
            broadcastMessage.setUserIds(unsentUserIds);
            log.info("[📨] SSE 发送主题订阅消息，topic:{},session keys:{},message:{}", SSE_TOPIC, unsentUserIds, sseMessage.getMessage());
            RedisUtil.publish(SSE_TOPIC, JsonUtil.toJson(broadcastMessage));
        }
    }

    /**
     * 向所有的用户发布订阅的消息 (群发)
     *
     * @param message 要发布的消息内容
     */
    public void publishAll(String message) {
        SseMessage broadcastMessage = new SseMessage();
        broadcastMessage.setMessage(message);
        log.info("[📨] SSE 发送主题订阅消息，topic:{},message:{}", SSE_TOPIC, message);
        RedisUtil.publish(SSE_TOPIC, JsonUtil.toJson(broadcastMessage));
    }

}