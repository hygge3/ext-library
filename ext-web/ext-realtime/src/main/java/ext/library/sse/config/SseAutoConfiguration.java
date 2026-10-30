package ext.library.sse.config;

import ext.library.sse.controller.SseController;
import ext.library.sse.listener.SseTopicListener;
import ext.library.sse.manager.SseConnectionManager;
import ext.library.sse.manager.SseEmitterManager;
import ext.library.sse.manager.SseHeartbeatManager;
import ext.library.sse.manager.SseMessagePublisher;
import ext.library.core.pubsub.PubSub;
import ext.library.sse.properties.SseProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.beans.factory.ObjectProvider;
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

    /**
     * SSE 消息发布器
     * <p>
     * 广播用的 {@link PubSub} 取自容器：引了 ext-redis 就是 {@code RedisPubSub}，引了 ext-postgres 就是
     * {@code PostgresPubSub}，本模块不再自带 pub/sub 实现。一个都没引时以单实例模式运行。
     */
    @Bean
    public SseMessagePublisher sseMessagePublisher(SseConnectionManager connectionManager, ObjectProvider<PubSub> pubSub, SseProperties properties) {
        return new SseMessagePublisher(connectionManager, pubSub.getIfAvailable(), properties.getTopic());
    }

    @Bean
    public SseEmitterManager sseEmitterManager(SseConnectionManager connectionManager, SseMessagePublisher messagePublisher, SseProperties properties) {
        Logs.info(EmojiSymbol.SSE, "载入模块：SSE，连接路径:{}", properties.getPath());
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
