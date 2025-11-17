package ext.library.tool.exception;


import ext.library.tool.util.StringUtil;

/**
 * 工具异常
 */
public class ToolException extends RuntimeException {

    /** 请求状态码 */
    private final Integer code;

    /**
     * 创建一个工具异常对象
     * <p>
     * 用于封装工具相关的异常信息，包含异常消息和错误代码
     *
     * @param message 异常消息模板
     * @param args    可变参数，用于替换消息中的占位符
     */
    public ToolException(String message, Object... args) {
        super(StringUtil.format(message, args));
        this.code = BizCode.TOOL_EXCEPTION.getCode();
    }

    /**
     * 构造函数，用于创建一个 ToolException 对象
     * <p>
     * 将给定的 Throwable 对象作为原因，并设置错误码为 TOOL_EXCEPTION 对应的代码
     *
     * @param throwable 异常原因
     */
    public ToolException(Throwable throwable) {
        super(throwable);
        this.code = BizCode.TOOL_EXCEPTION.getCode();
    }

    /**
     * 构造一个工具异常对象
     * <p>
     * 使用给定的异常信息，消息和参数初始化异常对象，并设置异常代码为工具异常对应的业务代码
     *
     * @param throwable 异常对象
     * @param message   异常消息模板
     * @param args      可变参数，用于替换消息模板中的占位符
     */
    public ToolException(Throwable throwable, String message, Object... args) {
        super(StringUtil.format(message, args), throwable);
        this.code = BizCode.TOOL_EXCEPTION.getCode();
    }

}