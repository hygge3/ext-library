package ext.library.security.handler;

import ext.library.security.exception.ForbiddenException;
import ext.library.security.exception.UnauthorizedException;
import ext.library.servlet.exception.ErrorEnvelope;
import ext.library.servlet.exception.ErrorLog;
import ext.library.servlet.response.R;
import ext.library.tool.constant.EmojiSymbol;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证异常处理器
 * <p>
 * 只声明"认证/授权异常归我管"，信封与日志口径复用 servlet 基座。
 */
@Order(0)
@AutoConfiguration
@RestControllerAdvice
public class SecurityExceptionHandler {

    /**
     * 权限码异常
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<R<Void>> forbiddenException(ForbiddenException e, HttpServletRequest request) {
        ErrorLog.warn(EmojiSymbol.SECURITY, request, e.getMessage());
        return ErrorEnvelope.of(HttpStatus.FORBIDDEN).toResponseEntity();
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<R<Void>> unauthorizedException(UnauthorizedException e, HttpServletRequest request) {
        ErrorLog.warn(EmojiSymbol.SECURITY, request, e.getMessage());
        return ErrorEnvelope.of(HttpStatus.UNAUTHORIZED).toResponseEntity();
    }

}
