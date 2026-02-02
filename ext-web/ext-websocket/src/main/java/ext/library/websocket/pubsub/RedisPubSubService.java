package ext.library.websocket.pubsub;

import ext.library.redis.util.RedisUtil;

import java.util.function.Consumer;

/**
 * 基于 Redis 的发布订阅服务实现
 */
public class RedisPubSubService implements PubSubService {

    @Override
    public void publish(String topic, String message) {
        RedisUtil.publish(topic, message);
    }

    @Override
    public <T> void subscribe(String topic, Class<T> clazz, Consumer<T> consumer) {
        RedisUtil.subscribe(topic, clazz, consumer);
    }

}
