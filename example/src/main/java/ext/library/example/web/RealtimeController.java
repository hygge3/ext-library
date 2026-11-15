package ext.library.example.web;

import ext.library.example.service.AuthService;
import ext.library.sse.util.SseUtil;
import ext.library.websocket.util.WebSocketUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实时通道演示：同一账号开着的页面会立刻收到消息。
 * <p>
 * SSE 单向推送（断开由浏览器负责），WebSocket 双向（这里只演示服务端推）。
 */
@RestController
@RequiredArgsConstructor
public class RealtimeController {

    private final AuthService authService;

    @PostMapping("/api/realtime/sse")
    public Map<String, Object> sse(@RequestParam(defaultValue = "来自服务端的 SSE 消息") String text) {
        SseUtil.sendMessage(authService.currentUser().getEmail(), text);
        return Map.of("sent", "sse", "text", text);
    }

    @PostMapping("/api/realtime/ws")
    public Map<String, Object> ws(@RequestParam(defaultValue = "来自服务端的 WebSocket 消息") String text) {
        WebSocketUtil.sendMessage(authService.currentUser().getEmail(), text);
        return Map.of("sent", "websocket", "text", text);
    }
}
