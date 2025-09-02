package ext.library.sse.config;

import ext.library.sse.config.properties.SseProperties;
import ext.library.sse.controller.SseController;
import ext.library.sse.listener.SseTopicListener;
import ext.library.sse.manager.SseEmitterManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * SSE 自动装配
 */
@Slf4j
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
        log.info("[📨] SSE 模块载入成功");
        return new SseController(sseEmitterManager);
    }

}