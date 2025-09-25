package ext.library.sse.config;

import ext.library.sse.config.properties.SseProperties;
import ext.library.sse.controller.SseController;
import ext.library.sse.listener.SseTopicListener;
import ext.library.sse.manager.SseEmitterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(getClass());

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