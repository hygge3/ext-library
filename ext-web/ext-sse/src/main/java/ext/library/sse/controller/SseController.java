package ext.library.sse.controller;

import ext.library.security.util.SecurityUtil;
import ext.library.sse.manager.SseEmitterManager;
import ext.library.sse.properties.SseProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE 控制器
 */
@Controller
@ConditionalOnProperty(value = SseProperties.PREFIX + ".enabled", havingValue = "true")
public class SseController {

    private final SseEmitterManager sseEmitterManager;

    public SseController(SseEmitterManager sseEmitterManager) {
        this.sseEmitterManager = sseEmitterManager;
    }

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "${ext.sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        String tokenValue = SecurityUtil.getCurrentTokenValue();
        String userId = SecurityUtil.getCurrentLoginId();
        return sseEmitterManager.connect(userId, tokenValue);
    }

    /**
     * 关闭 SSE 连接
     */
    @GetMapping("${ext.sse.path}/close")
    public void close() {
        String tokenValue = SecurityUtil.getCurrentTokenValue();
        String userId = SecurityUtil.getCurrentLoginId();
        sseEmitterManager.disconnect(userId, tokenValue);
    }

}
