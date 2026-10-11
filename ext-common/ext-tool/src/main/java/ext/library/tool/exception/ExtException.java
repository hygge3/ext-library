package ext.library.tool.exception;

import ext.library.tool.util.StringUtil;

import java.io.Serial;

/**
 * 框架内部异常
 * <p>
 * 用于框架各模块抛出的内部异常，自动添加模块标识前缀，便于定位问题来源。
 * <p>
 * 消息格式：{@code [module] message}
 *
 * @since 2025.01.01
 */
public class ExtException extends BizException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 使用模块标识和格式化消息构造异常
     *
     * @param module  模块标识（如 emoji 符号）
     * @param message 消息模板
     * @param args    格式化参数
     */
    public ExtException(String module, String message, Object... args) {
        super(formatMessage(module, StringUtil.format(message, args)));
    }

    /**
     * 使用模块标识和原因异常构造异常
     *
     * @param module 模块标识（如 emoji 符号）
     * @param cause  原因异常
     */
    public ExtException(String module, Throwable cause) {
        super(formatMessage(module, cause.getMessage()), cause);
    }

    /**
     * 使用模块标识、格式化消息和原因异常构造异常
     *
     * @param module  模块标识（如 emoji 符号）
     * @param cause   原因异常
     * @param message 消息模板
     * @param args    格式化参数
     */
    public ExtException(String module, Throwable cause, String message, Object... args) {
        super(formatMessage(module, StringUtil.format(message, args)), cause);
    }

    /**
     * 格式化异常消息，添加模块前缀
     *
     * @param module  模块标识
     * @param message 原始消息
     * @return 格式化后的消息
     */
    private static String formatMessage(String module, String message) {
        return "[" + module + "] " + message;
    }
}
