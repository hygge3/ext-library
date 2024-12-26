package ext.library.satoken.handler;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import ext.library.core.exception.BizCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * SaToken 异常处理器
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {
    /**
     * 打印日志
     *
     * @param request 请求
     * @param message 消息
     * @param e       e
     */
    private static void printLog(@NotNull HttpServletRequest request, @NotNull @Nls String message, @NotNull Exception e) {
        log.error("URI:{},{}", request.getRequestURI(), message, e);
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Map<String, Object> notPermissionException(@NotNull NotPermissionException e, @NotNull HttpServletRequest request) {
        printLog(request, "权限校验失败", e);
        return Map.of("code", BizCode.FORBIDDEN, "msg", "没有访问权限，请联系管理员授权");
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Map<String, Object> notRoleException(@NotNull NotRoleException e, @NotNull HttpServletRequest request) {
        printLog(request, "角色校验失败", e);
        return Map.of("code", BizCode.FORBIDDEN, "msg", "没有角色权限，请联系管理员授权");
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(NotLoginException.class)
    public Map<String, Object> notLoginException(@NotNull NotLoginException e, @NotNull HttpServletRequest request) {
        printLog(request, "认证校验失败", e);
        return Map.of("code", BizCode.UNAUTHORIZED, "msg", "认证失败，无法访问系统资源");
    }

}
