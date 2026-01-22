package ext.library.postgres.pubsub;

import ext.library.json.util.JsonUtil;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.DisposableBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * PostgreSQL 发布订阅管理器
 * <p>
 * 使用 PostgreSQL 原生的 LISTEN/NOTIFY 机制实现消息发布订阅
 *
 * @since 4.0.0
 */
public class PostgresPubSub implements DisposableBean {

    private final DataSource dataSource;
    private final Map<String, Consumer<String>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService listenerExecutor;
    private volatile boolean running = true;
    private Connection listenerConnection;

    public PostgresPubSub(DataSource dataSource) {
        this.dataSource = dataSource;
        this.listenerExecutor = Executors.newVirtualThreadPerTaskExecutor();
        startListenerThread();
    }

    /**
     * 发布消息
     *
     * @param channel 通道名称
     * @param message 消息内容（将自动序列化为 JSON）
     */
    public void publish(String channel, Object message) {
        String payload = message instanceof String s ? s : JsonUtil.toJson(message);
        String sql = "SELECT pg_notify(?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, channel);
            ps.setString(2, payload);
            ps.execute();
            Logs.debug(EmojiSymbol.POSTGRES, "发布消息: channel={}, payload长度={}", channel, payload.length());
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "发布消息失败: channel={}", channel);
            throw new RuntimeException("Failed to publish to channel: " + channel, e);
        }
    }

    /**
     * 订阅通道
     *
     * @param channel  通道名称
     * @param callback 消息回调
     */
    public void subscribe(String channel, Consumer<String> callback) {
        listeners.put(channel, callback);
        try {
            ensureListenerConnection();
            try (Statement stmt = listenerConnection.createStatement()) {
                stmt.execute("LISTEN " + sanitizeChannel(channel));
            }
            Logs.info(EmojiSymbol.POSTGRES, "订阅通道: {}", channel);
        } catch (SQLException e) {
            listeners.remove(channel);
            Logs.error(EmojiSymbol.POSTGRES, e, "订阅通道失败: {}", channel);
            throw new RuntimeException("Failed to subscribe to channel: " + channel, e);
        }
    }

    /**
     * 订阅通道（带类型反序列化）
     *
     * @param channel  通道名称
     * @param clazz    消息类型
     * @param callback 消息回调
     */
    public <T> void subscribe(String channel, Class<T> clazz, Consumer<T> callback) {
        subscribe(channel, payload -> callback.accept(JsonUtil.readObj(payload, clazz)));
    }

    /**
     * 订阅通道（使用 NotificationListener）
     *
     * @param channel  通道名称
     * @param listener 通知监听器
     */
    public void subscribe(String channel, NotificationListener listener) {
        subscribe(channel, payload -> listener.onNotification(channel, payload));
    }

    /**
     * 取消订阅
     *
     * @param channel 通道名称
     */
    public void unsubscribe(String channel) {
        listeners.remove(channel);
        try {
            if (listenerConnection != null && !listenerConnection.isClosed()) {
                try (Statement stmt = listenerConnection.createStatement()) {
                    stmt.execute("UNLISTEN " + sanitizeChannel(channel));
                }
            }
            Logs.info(EmojiSymbol.POSTGRES, "取消订阅: {}", channel);
        } catch (SQLException e) {
            Logs.warn(EmojiSymbol.POSTGRES, "取消订阅失败: {}", channel);
        }
    }

    /**
     * 检查是否已订阅指定通道
     *
     * @param channel 通道名称
     * @return 是否已订阅
     */
    public boolean isSubscribed(String channel) {
        return listeners.containsKey(channel);
    }

    /**
     * 获取当前订阅的通道列表
     *
     * @return 通道列表
     */
    public String[] getSubscribedChannels() {
        return listeners.keySet().toArray(new String[0]);
    }

    /**
     * 启动监听线程
     */
    private void startListenerThread() {
        listenerExecutor.submit(() -> {
            Logs.debug(EmojiSymbol.POSTGRES, "发布订阅监听线程已启动");
            while (running) {
                try {
                    ensureListenerConnection();
                    PGConnection pgConn = listenerConnection.unwrap(PGConnection.class);

                    // 轮询通知，超时时间 1 秒
                    PGNotification[] notifications = pgConn.getNotifications(1000);

                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            String channel = notification.getName();
                            String payload = notification.getParameter();
                            Consumer<String> listener = listeners.get(channel);
                            if (listener != null) {
                                // 在单独的虚拟线程中处理消息，避免阻塞监听
                                listenerExecutor.submit(() -> {
                                    try {
                                        listener.accept(payload);
                                    } catch (Exception e) {
                                        Logs.error(EmojiSymbol.POSTGRES, e, "处理通知失败: channel={}", channel);
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    if (running) {
                        Logs.error(EmojiSymbol.POSTGRES, e, "监听线程异常，尝试重新连接...");
                        reconnectListener();
                    }
                }
            }
            Logs.debug(EmojiSymbol.POSTGRES, "发布订阅监听线程已停止");
        });
    }

    /**
     * 确保监听连接可用
     */
    private synchronized void ensureListenerConnection() throws SQLException {
        if (listenerConnection == null || listenerConnection.isClosed()) {
            listenerConnection = dataSource.getConnection();
            listenerConnection.setAutoCommit(true);

            // 重新订阅所有通道
            for (String channel : listeners.keySet()) {
                try (Statement stmt = listenerConnection.createStatement()) {
                    stmt.execute("LISTEN " + sanitizeChannel(channel));
                }
            }
            Logs.debug(EmojiSymbol.POSTGRES, "监听连接已建立，重新订阅 {} 个通道", listeners.size());
        }
    }

    /**
     * 重新连接监听
     */
    private void reconnectListener() {
        try {
            if (listenerConnection != null) {
                listenerConnection.close();
            }
        } catch (SQLException ignored) {
        }
        listenerConnection = null;

        // 等待一段时间后重试
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 校验通道名称，防止 SQL 注入
     */
    private String sanitizeChannel(String channel) {
        // PostgreSQL 通道名称只允许字母、数字和下划线
        if (!channel.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            throw new IllegalArgumentException("无效的通道名称: " + channel + "。通道名称必须以字母或下划线开头，且只能包含字母、数字和下划线。");
        }
        return channel;
    }

    @Override
    public void destroy() {
        running = false;
        listenerExecutor.shutdown();
        try {
            if (!listenerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                listenerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            listenerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        try {
            if (listenerConnection != null && !listenerConnection.isClosed()) {
                listenerConnection.close();
            }
        } catch (SQLException ignored) {
        }
        Logs.info(EmojiSymbol.POSTGRES, "发布订阅管理器已关闭");
    }
}
