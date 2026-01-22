package ext.library.postgres.pubsub;

/**
 * PostgreSQL 通知监听器接口
 *
 * @since 4.0.0
 */
@FunctionalInterface
public interface NotificationListener {

    /**
     * 处理接收到的通知
     *
     * @param channel 通道名称
     * @param payload 消息内容
     */
    void onNotification(String channel, String payload);
}
