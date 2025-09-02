package ext.library.satoken.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import ext.library.tool.biz.exception.BizCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

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
    private static void printLog(@Nonnull HttpServletRequest request, String message, Exception e) {
        log.error("[🛡️] URI:{},{}", request.getRequestURI(), message, e);
    }

    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public Map<String, Object> notPermissionException(NotPermissionException e, HttpServletRequest request) {
        printLog(request, "权限校验失败", e);
        return Map.of("code", BizCode.FORBIDDEN, "msg", "没有访问权限，请联系管理员授权");
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Map<String, Object> notRoleException(NotRoleException e, HttpServletRequest request) {
        printLog(request, "角色校验失败", e);
        return Map.of("code", BizCode.FORBIDDEN, "msg", "没有角色权限，请联系管理员授权");
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(NotLoginException.class)
    public Map<String, Object> notLoginException(NotLoginException e, HttpServletRequest request) {
        printLog(request, "认证校验失败", e);
        return Map.of("code", BizCode.UNAUTHORIZED, "msg", "认证失败，无法访问系统资源");
    }

}