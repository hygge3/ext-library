package ext.library.sse.util;

import ext.library.core.util.SpringUtil;
import ext.library.sse.domain.SseMessage;
import ext.library.sse.manager.SseEmitterManager;
import ext.library.tool.holder.Lazy;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 工具类
 */
@Slf4j
@UtilityClass
public final class SseUtil {

    private final static Lazy<SseEmitterManager> MANAGER = Lazy.of(() -> SpringUtil.getBean(SseEmitterManager.class));

    /**
     * 向指定的 WebSocket 会话发送消息
     *
     * @param userId  要发送消息的用户 id
     * @param message 要发送的消息内容
     */
    public static void sendMessage(String userId, String message) {
        MANAGER.get().sendMessage(userId, message);
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param message 要发送的消息内容
     */
    public static void sendMessage(String message) {
        MANAGER.get().sendMessage(message);
    }

    /**
     * 发布 SSE 订阅消息
     *
     * @param sseMessage 要发布的 SSE 消息对象
     */
    public static void publishMessage(SseMessage sseMessage) {
        MANAGER.get().publishMessage(sseMessage);
    }

    /**
     * 向所有的用户发布订阅的消息 (群发)
     *
     * @param message 要发布的消息内容
     */
    public static void publishAll(String message) {
        MANAGER.get().publishAll(message);
    }

}