package ext.library.websocket.manager;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.websocket.properties.WebSocketProperties;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 心跳管理器，负责定时发送心跳和检测超时连接
 */
public class WebSocketHeartbeatManager {

    private final WebSocketConnectionManager connectionManager;
    private final WebSocketProperties.Heartbeat heartbeatConfig;
    private final ScheduledExecutorService scheduler;

    /**
     * 记录每个会话最后一次活动时间
     */
    private final Map<String, Instant> lastActivityMap = new ConcurrentHashMap<>();

    public WebSocketHeartbeatManager(WebSocketConnectionManager connectionManager, WebSocketProperties.Heartbeat heartbeatConfig) {
        this.connectionManager = connectionManager;
        this.heartbeatConfig = heartbeatConfig;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "websocket-heartbeat");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * 启动心跳任务
     */
    public void start() {
        if (!heartbeatConfig.isEnabled()) {
            Logs.info(EmojiSymbol.WEBSOCKET, "WebSocket 心跳检测已禁用");
            return;
        }

        int interval = heartbeatConfig.getInterval();
        scheduler.scheduleAtFixedRate(this::heartbeatTask, interval, interval, TimeUnit.SECONDS);
        Logs.info(EmojiSymbol.WEBSOCKET, "WebSocket 心跳检测已启动，间隔:{}秒，超时:{}秒", interval, heartbeatConfig.getTimeout());
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
     * 记录会话活动（收到消息时调用）
     *
     * @param sessionKey 会话键
     */
    public void recordActivity(String sessionKey) {
        lastActivityMap.put(sessionKey, Instant.now());
    }

    /**
     * 移除会话活动记录（连接关闭时调用）
     *
     * @param sessionKey 会话键
     */
    public void removeActivity(String sessionKey) {
        lastActivityMap.remove(sessionKey);
    }

    /**
     * 心跳任务：发送 Ping 并检测超时
     */
    private void heartbeatTask() {
        Instant now = Instant.now();
        int timeout = heartbeatConfig.getTimeout();

        for (String sessionKey : connectionManager.getAllSessionKeys()) {
            WebSocketSession session = connectionManager.getSession(sessionKey);
            if (session == null || !session.isOpen()) {
                continue;
            }

            // 检查是否超时
            Instant lastActivity = lastActivityMap.get(sessionKey);
            if (lastActivity != null && now.minusSeconds(timeout).isAfter(lastActivity)) {
                Logs.warn(EmojiSymbol.WEBSOCKET, "[心跳] session({}) 超时，关闭连接", sessionKey);
                closeSession(session, sessionKey);
                continue;
            }

            // 发送 Ping
            sendPing(session, sessionKey);
        }
    }

    /**
     * 发送 Ping 消息
     */
    private void sendPing(WebSocketSession session, String sessionKey) {
        try {
            session.sendMessage(new PingMessage());
            Logs.debug(EmojiSymbol.WEBSOCKET, "[心跳] 发送 Ping 到 session({})", sessionKey);
        } catch (IOException e) {
            Logs.warn(EmojiSymbol.WEBSOCKET, "[心跳] session({}) 发送 Ping 失败: {}", sessionKey, e.getMessage());
            closeSession(session, sessionKey);
        }
    }

    /**
     * 关闭会话
     */
    private void closeSession(WebSocketSession session, String sessionKey) {
        try {
            session.close();
        } catch (IOException e) {
            Logs.warn(EmojiSymbol.WEBSOCKET, "[心跳] session({}) 关闭失败: {}", sessionKey, e.getMessage());
        }
        connectionManager.removeSession(sessionKey);
        lastActivityMap.remove(sessionKey);
    }

}
