package ext.library.security.handler;

import ext.library.security.exception.ForbiddenException;
import ext.library.security.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 认证异常处理器
 */
@Order(0)
@AutoConfiguration
@RestControllerAdvice
public class SecurityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     * @param e       e
     */
    private static void printLog(@Nonnull HttpServletRequest request, String message, Exception e) {
        log.error("[🛡️] URI:{},{}", request.getRequestURI(), message, e);

    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public Map<String, Object> forbiddenException(ForbiddenException e, HttpServletRequest request) {
        printLog(request, "权限校验失败", e);
        return Map.of("code", HttpStatus.FORBIDDEN.value(), "msg", "没有访问权限，请联系管理员授权");
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Map<String, Object> unauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        printLog(request, "认证校验失败", e);
        return Map.of("code", HttpStatus.UNAUTHORIZED.value(), "msg", "认证失败，无法访问系统资源");
    }

}