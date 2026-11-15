package ext.library.example.support;

import ext.library.core.pubsub.PubSub;
import ext.library.json.util.JsonUtil;
import ext.library.sse.util.SseUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 笔记变更广播。
 * <p>
 * 发消息走 ext-core 的 {@link PubSub} SPI（这里由 ext-redis 的 {@code RedisPubSub} 实现），订阅端收到后
 * 只推给本机的 SSE 客户端——多实例部署时每台机器的浏览器都能看到，而不是只有写入那台。
 */
@Component
@RequiredArgsConstructor
public class NoteBroadcaster {

    public static final String TOPIC = "note:events";

    private final PubSub pubSub;

    public record NoteEvent(String owner, String title, String action) {
    }

    public void publish(String owner, String title, String action) {
        pubSub.publish(TOPIC, JsonUtil.toJson(new NoteEvent(owner, title, action)));
    }

    @PostConstruct
    void listen() {
        pubSub.subscribe(TOPIC, NoteEvent.class,
                event -> SseUtil.sendMessage(event.owner(), "笔记《" + event.title() + "》" + event.action()));
    }
}
