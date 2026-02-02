package ext.library.sse.manager;

import ext.library.sse.properties.SseProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * SSE 心跳管理器，负责定时发送心跳保持连接活跃
 * <p>
 * SSE 是单向通信，服务端无法接收客户端响应，因此心跳机制仅用于：
 * <ul>
 *   <li>保持连接活跃，防止代理/防火墙因空闲超时断开连接</li>
 *   <li>检测并清理已断开的连接</li>
 * </ul>
 */
public class SseHeartbeatManager {

    private final SseProperties.Heartbeat heartbeatConfig;
    private final ScheduledExecutorService scheduler;

    /**
     * 存储所有 emitter 的引用，用于心跳发送
     * key: "{userId}:{token}"
     */
    private final Map<String, SseEmitter> emitterRegistry = new ConcurrentHashMap<>();

    public SseHeartbeatManager(SseProperties.Heartbeat heartbeatConfig) {
        this.heartbeatConfig = heartbeatConfig;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "sse-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 启动心跳任务
     */
    public void start() {
        if (!heartbeatConfig.isEnabled()) {
            Logs.info(EmojiSymbol.SSE, "SSE 心跳检测已禁用");
            return;
        }

        int interval = heartbeatConfig.getInterval();
        scheduler.scheduleAtFixedRate(this::heartbeatTask, interval, interval, TimeUnit.SECONDS);
        Logs.info(EmojiSymbol.SSE, "SSE 心跳检测已启动，间隔:{}秒", interval);
    }

    /**
     * 停止心跳任务
     */
    public void stop() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 注册 emitter 用于心跳检测
     *
     * @param userId  用户 ID
     * @param token   连接令牌
     * @param emitter SSE emitter
     */
    public void register(String userId, String token, SseEmitter emitter) {
        String key = buildKey(userId, token);
        emitterRegistry.put(key, emitter);

        // 当连接关闭时自动移除
        emitter.onCompletion(() -> emitterRegistry.remove(key));
        emitter.onTimeout(() -> emitterRegistry.remove(key));
        emitter.onError(e -> emitterRegistry.remove(key));
    }

    /**
     * 移除 emitter 注册
     *
     * @param userId 用户 ID
     * @param token  连接令牌
     */
    public void unregister(String userId, String token) {
        emitterRegistry.remove(buildKey(userId, token));
    }

    /**
     * 心跳任务：向所有连接发送心跳注释
     */
    private void heartbeatTask() {
        for (Map.Entry<String, SseEmitter> entry : emitterRegistry.entrySet()) {
            SseEmitter emitter = entry.getValue();
            try {
                // 发送 SSE 注释作为心跳（注释不会触发客户端事件，仅保持连接）
                emitter.send(SseEmitter.event().comment("heartbeat"));
                Logs.debug(EmojiSymbol.SSE, "[心跳] 发送心跳到 {}", entry.getKey());
            } catch (IOException e) {
                Logs.warn(EmojiSymbol.SSE, "[心跳] {} 发送失败，移除连接: {}", entry.getKey(), e.getMessage());
                emitterRegistry.remove(entry.getKey());
                // 尝试完成 emitter
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // 忽略
                }
            }
        }
    }

    private String buildKey(String userId, String token) {
        return userId + ":" + token;
    }

}
