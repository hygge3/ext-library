package ext.library.tool.exception;

import java.io.Serial;

/**
 * 业务异常
 * <p>
 * 用于在业务逻辑中抛出可预期的异常，携带业务错误码和消息。
 *
 * @since 2025.01.01
 */
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private int code = BizCode.UNKNOWN.getCode();

    /**
     * 构造业务异常
     *
     * @param message 异常消息
     */
    public BizException(String message) {
        super(message);
    }

    /**
     * 构造业务异常
     *
     * @param message 异常消息
     * @param cause   原因异常
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 构造业务异常
     *
     * @param cause 原因异常
     */
    public BizException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造业务异常
     *
     * @param responseCode 响应码
     */
    public BizException(ResponseCode responseCode) {
        super(responseCode.getMsg());
        this.code = responseCode.getCode();
    }

    /**
     * 构造业务异常
     *
     * @param responseCode 响应码
     * @param message      自定义消息
     */
    public BizException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
    }

    /**
     * 构造业务异常
     *
     * @param code    错误码
     * @param message 异常消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码
     *
     * @return 业务错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置业务错误码
     *
     * @param code 业务错误码
     */
    public void setCode(int code) {
        this.code = code;
    }
}
