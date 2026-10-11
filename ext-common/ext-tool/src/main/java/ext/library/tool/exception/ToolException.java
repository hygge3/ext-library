package ext.library.tool.exception;

import java.io.Serial;

/**
 * 工具异常
 * <p>
 * 用于工具类方法抛出的异常，自动添加模块标识前缀，便于定位问题来源。
 * <p>
 * 消息格式：{@code [module] message}
 *
 * @since 2025.01.01
 */
public class ToolException extends ExtException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 使用模块标识和格式化消息构造异常
     *
     * @param module  模块标识（如 emoji 符号）
     * @param message 消息模板
     * @param args    格式化参数
     */
    public ToolException(String module, String message, Object... args) {
        super(module, message, args);
    }

    /**
     * 使用模块标识和原因异常构造异常
     *
     * @param module 模块标识（如 emoji 符号）
     * @param cause  原因异常
     */
    public ToolException(String module, Throwable cause) {
        super(module, cause);
    }

    /**
     * 使用模块标识、格式化消息和原因异常构造异常
     *
     * @param module  模块标识（如 emoji 符号）
     * @param cause   原因异常
     * @param message 消息模板
     * @param args    格式化参数
     */
    public ToolException(String module, Throwable cause, String message, Object... args) {
        super(module, cause, message, args);
    }
}
