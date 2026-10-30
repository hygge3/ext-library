package ext.library.websocket.config;

import ext.library.core.pubsub.PubSub;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.websocket.handler.ExtWebSocketHandler;
import ext.library.websocket.interceptor.ExtWebSocketInterceptor;
import ext.library.websocket.listener.WebSocketTopicListener;
import ext.library.websocket.manager.WebSocketConnectionManager;
import ext.library.websocket.manager.WebSocketHeartbeatManager;
import ext.library.websocket.manager.WebSocketMessagePublisher;
import ext.library.websocket.properties.WebSocketProperties;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 自动装配
 */
@AutoConfiguration
@ConditionalOnProperty(value = WebSocketProperties.PREFIX + ".enabled", havingValue = "true")
@EnableConfigurationProperties(WebSocketProperties.class)
@EnableWebSocket
public class WebSocketAutoConfiguration {

    @Bean
    public WebSocketConnectionManager webSocketConnectionManager() {
        return new WebSocketConnectionManager();
    }

    @Bean
    public WebSocketHeartbeatManager webSocketHeartbeatManager(WebSocketConnectionManager connectionManager, WebSocketProperties properties) {
        WebSocketHeartbeatManager heartbeatManager = new WebSocketHeartbeatManager(connectionManager, properties.getHeartbeat());
        heartbeatManager.start();
        return heartbeatManager;
    }

    /**
     * WebSocket 消息发布器
     * <p>
     * 与 SSE 侧共用同一个 {@link PubSub} Bean（由 ext-redis 或 ext-postgres 提供），不再各自复制一份实现；
     * 容器里没有它时以单实例模式运行。
     */
    @Bean
    public WebSocketMessagePublisher webSocketMessagePublisher(WebSocketConnectionManager connectionManager, ObjectProvider<PubSub> pubSub, WebSocketProperties properties) {
        return new WebSocketMessagePublisher(connectionManager, pubSub.getIfAvailable(), properties.getTopic());
    }

    @Bean
    public WebSocketConfigurer webSocketConfigurer(HandshakeInterceptor handshakeInterceptor, WebSocketHandler webSocketHandler, @NonNull WebSocketProperties properties) {
        Logs.info(EmojiSymbol.WEBSOCKET, "载入模块：WebSocket，连接路径:{}", properties.getPath());
        return registry -> registry.addHandler(webSocketHandler, properties.getPath()).addInterceptors(handshakeInterceptor).setAllowedOrigins(properties.getAllowedOrigins());
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new ExtWebSocketInterceptor();
    }

    @Bean
    public WebSocketHandler webSocketHandler(WebSocketConnectionManager connectionManager, WebSocketHeartbeatManager heartbeatManager, WebSocketProperties properties) {
        return new ExtWebSocketHandler(connectionManager, heartbeatManager, properties);
    }

    @Bean
    public WebSocketTopicListener webSocketTopicListener() {
        return new WebSocketTopicListener();
    }

}
