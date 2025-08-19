package ext.library.websocket.config;

import ext.library.tool.util.StringUtil;
import ext.library.websocket.config.properties.WebSocketProperties;
import ext.library.websocket.handler.ExtWebSocketHandler;
import ext.library.websocket.interceptor.ExtWebSocketInterceptor;
import ext.library.websocket.listener.WebSocketTopicListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.annotation.Nonnull;

/**
 * WebSocket 配置
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(value = WebSocketProperties.PREFIX + ".enabled", havingValue = "true")
@EnableConfigurationProperties(WebSocketProperties.class)
@EnableWebSocket
public class WebSocketAutoConfig {

    @Bean
    public WebSocketConfigurer webSocketConfigurer(HandshakeInterceptor handshakeInterceptor, WebSocketHandler webSocketHandler, @Nonnull WebSocketProperties webSocketProperties) {
        // 如果 WebSocket 的路径为空，则设置默认路径为 "/websocket"
        if (StringUtil.isBlank(webSocketProperties.getPath())) {
            webSocketProperties.setPath("/websocket");
        }

        // 如果允许跨域访问的地址为空，则设置为 "*"，表示允许所有来源的跨域请求
        if (StringUtil.isBlank(webSocketProperties.getAllowedOrigins())) {
            webSocketProperties.setAllowedOrigins("*");
        }
        log.info("[⛓️] 注册 WebSocketHandler, 连接路径:{}", webSocketProperties.getPath());

        // 返回一个 WebSocketConfigurer 对象，用于配置 WebSocket
        return registry -> registry
                // 添加 WebSocket 处理程序和拦截器到指定路径，设置允许的跨域来源
                .addHandler(webSocketHandler, webSocketProperties.getPath()).addInterceptors(handshakeInterceptor).setAllowedOrigins(webSocketProperties.getAllowedOrigins());
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new ExtWebSocketInterceptor();
    }

    @Bean
    @ConditionalOnProperty(value = WebSocketProperties.PREFIX + ".enabled", havingValue = "true")
    public WebSocketHandler webSocketHandler() {
        return new ExtWebSocketHandler();
    }

    @Bean
    public WebSocketTopicListener topicListener() {
        return new WebSocketTopicListener();
    }

}