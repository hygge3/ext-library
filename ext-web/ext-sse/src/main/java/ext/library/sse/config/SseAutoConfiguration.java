package ext.library.sse.config;

import ext.library.sse.controller.SseController;
import ext.library.sse.listener.SseTopicListener;
import ext.library.sse.manager.SseConnectionManager;
import ext.library.sse.manager.SseEmitterManager;
import ext.library.sse.manager.SseHeartbeatManager;
import ext.library.sse.manager.SseMessagePublisher;
import ext.library.sse.properties.SseProperties;
import ext.library.sse.properties.SseProperties.PubSubBackend;
import ext.library.sse.pubsub.PostgresPubSubService;
import ext.library.sse.pubsub.PubSubService;
import ext.library.sse.pubsub.RedisPubSubService;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * SSE 自动装配
 */
@AutoConfiguration
@ConditionalOnProperty(value = SseProperties.PREFIX + ".enabled", havingValue = "true")
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    @Bean
    public SseConnectionManager sseConnectionManager() {
        return new SseConnectionManager();
    }

    @Bean
    public SseHeartbeatManager sseHeartbeatManager(SseConnectionManager connectionManager, SseProperties properties) {
        SseHeartbeatManager heartbeatManager = new SseHeartbeatManager(properties.getHeartbeat());
        // 将心跳管理器注入到连接管理器
        connectionManager.setHeartbeatManager(heartbeatManager);
        heartbeatManager.start();
        return heartbeatManager;
    }

    @Bean
    public PubSubService ssePubSubService(SseProperties properties) {
        if (properties.getBackend() == PubSubBackend.POSTGRES) {
            Logs.info(EmojiSymbol.SSE, "SSE 使用 PostgreSQL LISTEN/NOTIFY 作为发布订阅后端");
            return new PostgresPubSubService();
        }
        Logs.info(EmojiSymbol.SSE, "SSE 使用 Redis 作为发布订阅后端");
        return new RedisPubSubService();
    }

    @Bean
    public SseMessagePublisher sseMessagePublisher(SseConnectionManager connectionManager, PubSubService pubSubService, SseProperties properties) {
        return new SseMessagePublisher(connectionManager, pubSubService, properties.getTopic());
    }

    @Bean
    public SseEmitterManager sseEmitterManager(SseConnectionManager connectionManager, SseMessagePublisher messagePublisher, SseProperties properties) {
        Logs.info(EmojiSymbol.SSE, "载入模块:SSE，连接路径:{}", properties.getPath());
        return new SseEmitterManager(connectionManager, messagePublisher);
    }

    @Bean
    public SseTopicListener sseTopicListener() {
        return new SseTopicListener();
    }

    @Bean
    public SseController sseController(SseEmitterManager sseEmitterManager) {
        return new SseController(sseEmitterManager);
    }

}
