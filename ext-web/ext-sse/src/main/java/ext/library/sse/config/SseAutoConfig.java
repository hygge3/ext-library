package ext.library.sse.config;

import ext.library.sse.controller.SseController;
import ext.library.sse.listener.SseTopicListener;
import ext.library.sse.manager.SseEmitterManager;
import ext.library.sse.properties.SseProperties;
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
public class SseAutoConfig {

    @Bean
    public SseEmitterManager sseEmitterManager() {
        return new SseEmitterManager();
    }

    @Bean
    public SseTopicListener sseTopicListener() {
        return new SseTopicListener();
    }

    @Bean
    public SseController sseController(SseEmitterManager sseEmitterManager) {
        Logs.info(EmojiSymbol.SSE, "载入模块:SSE");
        return new SseController(sseEmitterManager);
    }

}
