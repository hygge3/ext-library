package ext.library.sse.controller;

import ext.library.security.util.SecurityUtil;
import ext.library.sse.config.properties.SseProperties;
import ext.library.sse.domain.SseMessage;
import ext.library.sse.manager.SseEmitterManager;
import ext.library.web.annotation.RestWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * SSE 控制器
 */
@RestController
@ConditionalOnProperty(value = SseProperties.PREFIX + ".enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    private final SseEmitterManager sseEmitterManager;

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
    @RestWrapper
    @GetMapping("${ext.sse.path}/close")
    public void close() {
        String tokenValue = SecurityUtil.getCurrentTokenValue();
        String userId = SecurityUtil.getCurrentLoginId();
        sseEmitterManager.disconnect(userId, tokenValue);
    }

    /**
     * 向特定用户发送消息
     *
     * @param userId 目标用户的 ID
     * @param msg    要发送的消息内容
     */
    @GetMapping("${ext.sse.path}/send")
    @RestWrapper
    public void send(@RequestParam String userId, @RequestParam String msg) {
        SseMessage dto = new SseMessage();
        dto.setUserIds(List.of(userId));
        dto.setMessage(msg);
        sseEmitterManager.publishMessage(dto);
    }

    /**
     * 向所有用户发送消息
     *
     * @param msg 要发送的消息内容
     */
    @GetMapping("${ext.sse.path}/sendAll")
    @RestWrapper
    public void send(@RequestParam String msg) {
        sseEmitterManager.publishAll(msg);
    }

    /**
     * 清理资源。此方法目前不执行任何操作，但避免因未实现而导致错误
     */
    @Override
    public void destroy() throws Exception {
        // 销毁时不需要做什么 此方法避免无用操作报错
    }

}