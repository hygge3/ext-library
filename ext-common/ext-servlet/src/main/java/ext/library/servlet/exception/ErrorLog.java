package ext.library.servlet.exception;

import ext.library.tool.runtime.Logs;
import org.jspecify.annotations.Nullable;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 统一错误日志
 * <p>
 * 异常处理时"记什么、怎么记"的口径集中在这里（模块标识 + 请求 URI + 消息），
 * 各模块的异常处理器不再各自拼 {@code "URI:{},{}"} 的格式串。
 *
 * @since 4.1.0
 */
public final class ErrorLog {

    /** 统一的日志格式：请求 URI + 消息 */
    private static final String LOG_PATTERN = "URI:{},{}";

    private ErrorLog() {
    }

    /**
     * 记录错误日志（带异常堆栈）
     *
     * @param module  模块标识，取自 {@code EmojiSymbol}
     * @param request 当前请求，可为 {@code null}
     * @param message 消息
     * @param e       异常
     */
    public static void error(String module, @Nullable HttpServletRequest request, String message, Throwable e) {
        Logs.error(module, e, LOG_PATTERN, uri(request), message);
    }

    /**
     * 记录错误日志
     *
     * @param module  模块标识，取自 {@code EmojiSymbol}
     * @param request 当前请求，可为 {@code null}
     * @param message 消息
     */
    public static void error(String module, @Nullable HttpServletRequest request, String message) {
        Logs.error(module, LOG_PATTERN, uri(request), message);
    }

    /**
     * 记录告警日志
     *
     * @param module  模块标识，取自 {@code EmojiSymbol}
     * @param request 当前请求，可为 {@code null}
     * @param message 消息
     */
    public static void warn(String module, @Nullable HttpServletRequest request, String message) {
        Logs.warn(module, LOG_PATTERN, uri(request), message);
    }

    /**
     * 记录告警日志（带异常堆栈）
     *
     * @param module  模块标识，取自 {@code EmojiSymbol}
     * @param request 当前请求，可为 {@code null}
     * @param message 消息
     * @param e       异常
     */
    public static void warn(String module, @Nullable HttpServletRequest request, String message, Throwable e) {
        Logs.warn(module, e, LOG_PATTERN, uri(request), message);
    }

    /**
     * 取请求 URI，非 Web 上下文时用占位符
     *
     * @param request 当前请求
     *
     * @return 请求 URI
     */
    private static String uri(@Nullable HttpServletRequest request) {
        return request == null ? "-" : request.getRequestURI();
    }

}
