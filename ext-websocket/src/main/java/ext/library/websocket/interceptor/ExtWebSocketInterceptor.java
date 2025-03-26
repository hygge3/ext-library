package ext.library.websocket.interceptor;

import jakarta.annotation.Nonnull;

import ext.library.security.exception.UnauthorizedException;
import ext.library.security.util.SecurityUtil;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import static ext.library.websocket.constant.WebSocketConstants.LOGIN_USER_KEY;

/**
 * WebSocket 握手请求的拦截器
 */
@Slf4j
public class ExtWebSocketInterceptor implements HandshakeInterceptor {

    /**
     * WebSocket 握手之前执行的前置处理方法
     *
     * @param request    WebSocket 握手请求
     * @param response   WebSocket 握手响应
     * @param wsHandler  WebSocket 处理程序
     * @param attributes 与 WebSocket 会话关联的属性
     * @return 如果允许握手继续进行，则返回 true；否则返回 false
     */
    @Override
    public boolean beforeHandshake(@Nonnull  ServerHttpRequest request, @Nonnull  ServerHttpResponse response, @Nonnull WebSocketHandler wsHandler,
                                   @Nonnull Map<String, Object> attributes) {
        try {
            // 检查是否登录 是否有 token
            SecurityUtil.checkToken();
            attributes.put(LOGIN_USER_KEY, SecurityUtil.getCurrentSecuritySession());
            return true;
        } catch (UnauthorizedException e) {
            log.error("[⛓️] WebSocket 认证失败'{}',无法访问系统资源", e.getMessage());
            return false;
        }
    }

    /**
     * WebSocket 握手成功后执行的后置处理方法
     *
     * @param request   WebSocket 握手请求
     * @param response  WebSocket 握手响应
     * @param wsHandler WebSocket 处理程序
     * @param exception 握手过程中可能出现的异常
     */
    @Override
    public void afterHandshake(@Nonnull  ServerHttpRequest request, @Nonnull  ServerHttpResponse response, @Nonnull  WebSocketHandler wsHandler,
                               Exception exception) {
        // 在这个方法中可以执行一些握手成功后的后续处理逻辑，比如记录日志或者其他操作
        log.info("[⛓️] WebSocket 连接成功 '{}'", SecurityUtil.getCurrentLoginId());
    }

}
