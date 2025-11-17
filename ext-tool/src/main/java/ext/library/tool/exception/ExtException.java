package ext.library.tool.exception;


import ext.library.tool.util.StringUtil;

/**
 * 框架内部异常
 */
public class ExtException extends RuntimeException {

    /** 请求状态码 */
    private final Integer code;

    /**
     * 构造一个框架内部异常对象，用于封装工具类相关的异常信息
     * <p>
     * 使用指定的消息和参数格式化异常信息，并设置异常代码为工具异常对应的业务代码
     *
     * @param message 异常消息模板
     * @param args    用于格式化消息的参数列表
     */
    public ExtException(String message, Object... args) {
        super(StringUtil.format(message, args));
        this.code = BizCode.TOOL_EXCEPTION.getCode();
    }

    /**
     * 构造一个框架内部异常对象，用于封装工具类异常
     * <p>
     * 将传入的 Throwable 异常作为原因，并设置统一的异常码
     *
     * @param throwable 异常原因
     *
     * @since 1.0
     */
    public ExtException(Throwable throwable) {
        super(throwable);
        this.code = BizCode.TOOL_EXCEPTION.getCode();
    }

    /**
     * 构造一个框架内部异常对象，用于封装工具类相关的异常信息
     * <p>
     * 将给定的消息格式化后作为异常信息，并将原始异常作为原因进行封装。
     *
     * @param throwable 原始异常对象
     * @param message   异常消息模板
     * @param args      格式化消息的参数
     */
    public ExtException(Throwable throwable, String message, Object... args) {
        super(StringUtil.format(message, args), throwable);
        this.code = BizCode.TOOL_EXCEPTION.getCode();
    }
}