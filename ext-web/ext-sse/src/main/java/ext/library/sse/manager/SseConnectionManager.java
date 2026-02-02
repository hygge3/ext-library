package ext.library.sse.manager;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.runtime.VirtualThreadPools;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 连接管理器，负责连接的建立、断开和本地消息发送
 */
public class SseConnectionManager {

    private static final Map<String, Map<String, SseEmitter>> userTokenEmitters = new ConcurrentHashMap<>();

    /**
     * 建立与指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     *
     * @return 返回一个 SseEmitter 实例，客户端可以通过该实例接收 SSE 事件
     */
    public SseEmitter connect(String userId, String token) {
        // 获取或创建当前用户的 SseEmitter 映射表
        Map<String, SseEmitter> emitters = userTokenEmitters.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        // 创建一个新的 SseEmitter 实例，超时时间设置为 0 表示无限制
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(token, emitter);

        // 当 emitter 完成、超时或发生错误时，从映射表中移除对应的 token
        emitter.onCompletion(() -> emitters.remove(token));
        emitter.onTimeout(() -> emitters.remove(token));
        emitter.onError(e -> {
            Logs.warn(EmojiSymbol.SSE, "SSE 连接异常，userId:{}, token:{}, error:{}", userId, token, e.getMessage());
            emitters.remove(token);
        });

        try {
            emitter.send(SseEmitter.event().comment("connected").data("connected"));
        } catch (IOException e) {
            Logs.warn(EmojiSymbol.SSE, "SSE 连接初始化失败，userId:{}, token:{}, error:{}", userId, token, e.getMessage());
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
        Map<String, SseEmitter> emitters = userTokenEmitters.get(userId);
        if (emitters != null) {
            try {
                SseEmitter emitter = emitters.get(token);
                if (emitter != null) {
                    emitter.send(SseEmitter.event().comment("disconnected").data("disconnected"));
                    emitter.complete();
                }
            } catch (Exception e) {
                Logs.warn(EmojiSymbol.SSE, "SSE 断开连接异常，userId:{}, token:{}, error:{}", userId, token, e.getMessage());
            } finally {
                emitters.remove(token);
            }
        }
    }

    /**
     * 检查用户是否在当前服务实例中有连接
     *
     * @param userId 用户 ID
     *
     * @return 是否存在连接
     */
    public boolean hasConnection(String userId) {
        return userTokenEmitters.containsKey(userId);
    }

    /**
     * 向指定用户的所有会话发送消息
     *
     * @param userId  要发送消息的用户 ID
     * @param message 要发送的消息内容
     */
    public void sendMessage(String userId, String message) {
        VirtualThreadPools.execute("SSE Send", () -> {
            Map<String, SseEmitter> emitters = userTokenEmitters.get(userId);
            if (emitters != null) {
                for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                    try {
                        entry.getValue().send(SseEmitter.event().name("message").data(message));
                    } catch (Exception e) {
                        Logs.warn(EmojiSymbol.SSE, "SSE 发送消息失败，userId:{}, token:{}, error:{}", userId, entry.getKey(), e.getMessage());
                        emitters.remove(entry.getKey());
                    }
                }
            }
        });
    }

    /**
     * 向本机所有用户会话发送消息
     *
     * @param message 要发送的消息内容
     */
    public void sendMessageToAll(String message) {
        for (String userId : userTokenEmitters.keySet()) {
            sendMessage(userId, message);
        }
    }

}
