package ext.library.tool.exception;


import ext.library.tool.response.ResponseCode;
import ext.library.tool.util.StringUtil;

/**
 * 业务异常
 */
public class BizException extends RuntimeException {

    private final Integer code;

    /**
     * 异常提示消息构造
     *
     * @param message 消息
     * @param args    参数
     */
    public BizException(String message, Object... args) {
        super(StringUtil.format(message, args));
        this.code = BizCode.WARN.getCode();
    }

    /**
     * 异常提示消息构造
     *
     * @param message 消息
     * @param args    参数
     */
    public BizException(Throwable throwable, String message, Object... args) {
        super(StringUtil.format(message, args), throwable);
        this.code = BizCode.WARN.getCode();
    }

    /**
     * 异常提示消息构造
     *
     * @param message 消息
     * @param args    参数
     */
    public BizException(Throwable throwable, ResponseCode resultCode, String message, Object... args) {
        super(StringUtil.format(message, args), throwable);
        this.code = resultCode.getCode();
    }

    public Integer getCode() {
        return code;
    }

}