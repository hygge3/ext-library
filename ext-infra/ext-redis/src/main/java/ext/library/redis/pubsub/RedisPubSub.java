package ext.library.redis.pubsub;

import ext.library.core.pubsub.PubSub;
import ext.library.json.util.JsonUtil;
import ext.library.redis.util.RedisUtil;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.function.Consumer;

/**
 * 基于 Redis 的发布订阅实现
 *
 * <p>发布直接走 {@link RedisUtil#publish(String, String)}，订阅用 {@code RedisMessageListenerContainer}
 * 注册监听器并把消息体按指定类型反序列化，避免各处重复写 JSON 转换。
 */
public class RedisPubSub implements PubSub {

    private final RedisMessageListenerContainer container;

    public RedisPubSub(RedisMessageListenerContainer container) {
        this.container = container;
    }

    @Override
    public void publish(String topic, String message) {
        RedisUtil.publish(topic, message);
    }

    @Override
    public <T> void subscribe(String topic, Class<T> clazz, Consumer<T> consumer) {
        subscribe(new ChannelTopic(topic), clazz, consumer);
    }

    /**
     * 按模式订阅，支持通配符 {@code *}（任意多个字符）与 {@code ?}（单个字符）
     *
     * @param pattern  频道模式，如 {@code ext:sse:*}、{@code ext:sse:topic?.message}
     * @param clazz    消息类型
     * @param consumer 消息消费者
     * @param <T>      消息类型泛型
     *
     * @return 订阅句柄，{@link Subscription#close()} 后不再接收该模式的频道消息
     */
    public <T> Subscription subscribePattern(String pattern, Class<T> clazz, Consumer<T> consumer) {
        return subscribe(new PatternTopic(pattern), clazz, consumer);
    }

    private <T> Subscription subscribe(Topic topic, Class<T> clazz, Consumer<T> consumer) {
        MessageListener listener = (message, pattern) -> consumer.accept(JsonUtil.readObj(new String(message.getBody()), clazz));
        container.addMessageListener(listener, topic);
        return new Subscription(container, listener, topic);
    }

    /**
     * 订阅句柄，关闭后停止接收该主题的消息
     *
     * @param container 监听容器
     * @param listener  监听器
     * @param topic     主题
     */
    public record Subscription(RedisMessageListenerContainer container, MessageListener listener, Topic topic) implements AutoCloseable {

        @Override
        public void close() {
            container.removeMessageListener(listener, topic);
        }

    }

}
