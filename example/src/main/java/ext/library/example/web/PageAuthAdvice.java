package ext.library.example.web;

import ext.library.security.exception.UnauthorizedException;
import ext.library.servlet.exception.ErrorEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 未登录时的页面跳转
 * <p>
 * 浏览器直接访问页面（{@code Accept} 含 {@code text/html}）未登录，302 到登录页；
 * 前端 fetch / SSE 这类请求保持库里的 401 JSON 信封。
 * 优先级高于 ext-security 的 {@code SecurityExceptionHandler}，否则到不了这里。
 */
@Order(-1)
@RestControllerAdvice
public class PageAuthAdvice {

    private static final URI LOGIN = URI.create("/login");

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> unauthorized(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/html")) {
            return ResponseEntity.status(HttpStatus.FOUND).location(LOGIN).build();
        }
        return ErrorEnvelope.of(HttpStatus.UNAUTHORIZED).toResponseEntity();
    }
}
