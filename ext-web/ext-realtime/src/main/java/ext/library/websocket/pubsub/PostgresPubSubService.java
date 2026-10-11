package ext.library.websocket.pubsub;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.util.PostgresUtil;

import java.util.function.Consumer;

/**
 * 基于 PostgreSQL LISTEN/NOTIFY 的发布订阅服务实现
 */
public class PostgresPubSubService implements PubSubService {

    @Override
    public void publish(String topic, String message) {
        PostgresUtil.publish(topic, message);
    }

    @Override
    public <T> void subscribe(String topic, Class<T> clazz, Consumer<T> consumer) {
        PostgresUtil.subscribe(topic, payload -> {
            T obj = JsonUtil.readObj(payload, clazz);
            consumer.accept(obj);
        });
    }

}
