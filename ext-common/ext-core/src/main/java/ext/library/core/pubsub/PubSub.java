package ext.library.core.pubsub;

import java.util.function.Consumer;

/**
 * 发布订阅 SPI
 *
 * <p>抽象"把一条消息广播给所有实例"这一能力，具体后端由 {@code ext-redis}（{@code RedisPubSub}）或
 * {@code ext-postgres}（{@code PostgresPubSub}，基于 {@code LISTEN/NOTIFY}）提供。
 * 消息以 JSON 字符串传输，订阅端按给定类型反序列化。
 */
public interface PubSub {

    /**
     * 发布消息到指定主题
     *
     * @param topic   主题 / 频道名称
     * @param message 消息内容（JSON 字符串）
     */
    void publish(String topic, String message);

    /**
     * 订阅指定主题的消息
     *
     * @param topic    主题 / 频道名称
     * @param clazz    消息类型
     * @param consumer 消息消费者
     * @param <T>      消息类型泛型
     */
    <T> void subscribe(String topic, Class<T> clazz, Consumer<T> consumer);

}
