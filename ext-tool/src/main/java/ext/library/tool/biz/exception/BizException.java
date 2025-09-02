package ext.library.tool.biz.exception;


import ext.library.tool.biz.response.ResponseCode;
import ext.library.tool.util.StringUtil;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final Integer code;

    /**
     * 异常提示消息构造
     *
     * @param resultCode 结果代码
     */
    public BizException(ResponseCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 异常提示消息构造
     *
     * @param resultCode 结果代码
     * @param message    消息
     */
    public BizException(ResponseCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 异常提示消息构造
     *
     * @param resultCode 结果代码
     * @param message    消息
     */
    public BizException(ResponseCode resultCode, String message, Object... args) {
        super(StringUtil.format(message, args));
        this.code = resultCode.getCode();
    }

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

}