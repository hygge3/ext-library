package ext.library.security.handler;

import ext.library.security.exception.ForbiddenException;
import ext.library.security.exception.UnauthorizedException;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.web.response.R;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证异常处理器
 */
@Order(0)
@AutoConfiguration
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     */
    private static void printLog(@Nonnull HttpServletRequest request, String message) {
        Logs.warn(EmojiSymbol.SECURITY, "URI:{},{}", request.getRequestURI(), message);
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> forbiddenException(ForbiddenException e, HttpServletRequest request) {
        printLog(request, e.getMessage());
        return R.failed(HttpStatus.FORBIDDEN);
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> unauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        printLog(request, e.getMessage());
        return R.failed(HttpStatus.UNAUTHORIZED);
    }

}
