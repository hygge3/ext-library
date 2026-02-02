package ext.library.sse.pubsub;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.pubsub.PostgresPubSub;

import java.util.function.Consumer;

/**
 * 基于 PostgreSQL LISTEN/NOTIFY 的发布订阅服务实现
 */
public class PostgresPubSubService implements PubSubService {

    private final PostgresPubSub postgresPubSub;

    public PostgresPubSubService(PostgresPubSub postgresPubSub) {
        this.postgresPubSub = postgresPubSub;
    }

    @Override
    public void publish(String topic, String message) {
        postgresPubSub.publish(topic, message);
    }

    @Override
    public <T> void subscribe(String topic, Class<T> clazz, Consumer<T> consumer) {
        postgresPubSub.subscribe(topic, payload -> {
            T obj = JsonUtil.readObj(payload, clazz);
            consumer.accept(obj);
        });
    }

}
