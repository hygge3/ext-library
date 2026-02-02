package ext.library.sse.pubsub;

import java.util.function.Consumer;

/**
 * 发布订阅服务接口，抽象 Redis 和 PostgreSQL 的发布订阅操作
 */
public interface PubSubService {

    /**
     * 发布消息到指定主题
     *
     * @param topic   主题/频道名称
     * @param message 消息内容（JSON 字符串）
     */
    void publish(String topic, String message);

    /**
     * 订阅指定主题的消息
     *
     * @param topic    主题/频道名称
     * @param clazz    消息类型
     * @param consumer 消息消费者
     * @param <T>      消息类型泛型
     */
    <T> void subscribe(String topic, Class<T> clazz, Consumer<T> consumer);

}
