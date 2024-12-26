package ext.library.security.handler;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import ext.library.core.exception.BizCode;
import ext.library.security.exception.ForbiddenException;
import ext.library.security.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 认证异常处理器
 */
@Slf4j
@Order(0)
@AutoConfiguration
@RestControllerAdvice
public class SecurityExceptionHandler {
    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     * @param e       e
     */
    private static void printLog(@NotNull HttpServletRequest request, @NotNull @Nls String message, @NotNull Exception e) {
        log.error("[⚠️] URI:{},{}", request.getRequestURI(), message, e);

    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public Map<String, Object> forbiddenException(@NotNull ForbiddenException e, @NotNull HttpServletRequest request) {
        printLog(request, "权限校验失败", e);
        return Map.of("code", BizCode.FORBIDDEN.getCode(), "msg", "没有访问权限，请联系管理员授权");
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(UnauthorizedException.class)
    public Map<String, Object> unauthorizedException(@NotNull UnauthorizedException e, @NotNull HttpServletRequest request) {
        printLog(request, "认证校验失败", e);
        return Map.of("code", BizCode.UNAUTHORIZED.getCode(), "msg", "认证失败，无法访问系统资源");
    }

}
