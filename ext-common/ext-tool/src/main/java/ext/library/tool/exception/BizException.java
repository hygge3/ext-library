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
    private final int code;

    /**
     * 使用消息构造异常（错误码默认为 UNKNOWN）
     *
     * @param message 异常消息
     */
    public BizException(String message) {
        super(message);
        this.code = BizCode.UNKNOWN.getCode();
    }

    /**
     * 使用消息和原因构造异常（错误码默认为 UNKNOWN）
     *
     * @param message 异常消息
     * @param cause   原因异常
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = BizCode.UNKNOWN.getCode();
    }

    /**
     * 使用原因构造异常（错误码默认为 UNKNOWN，消息取自原因）
     *
     * @param cause 原因异常
     */
    public BizException(Throwable cause) {
        super(cause);
        this.code = BizCode.UNKNOWN.getCode();
    }

    /**
     * 使用响应码构造异常
     *
     * @param responseCode 响应码（提供错误码和消息）
     */
    public BizException(ResponseCode responseCode) {
        super(responseCode.getMsg());
        this.code = responseCode.getCode();
    }

    /**
     * 使用响应码和原因构造异常
     *
     * @param responseCode 响应码（提供错误码）
     * @param cause        原因异常
     */
    public BizException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMsg(), cause);
        this.code = responseCode.getCode();
    }

    /**
     * 使用响应码和自定义消息构造异常
     *
     * @param responseCode 响应码（提供错误码）
     * @param message      自定义消息（覆盖响应码的默认消息）
     */
    public BizException(ResponseCode responseCode, String message) {
        super(message);
        this.code = responseCode.getCode();
    }

    /**
     * 使用响应码、自定义消息和原因构造异常
     *
     * @param responseCode 响应码（提供错误码）
     * @param message      自定义消息
     * @param cause        原因异常
     */
    public BizException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.code = responseCode.getCode();
    }

    /**
     * 使用错误码和消息构造异常
     *
     * @param code    错误码
     * @param message 异常消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用错误码、消息和原因构造异常
     *
     * @param code    错误码
     * @param message 异常消息
     * @param cause   原因异常
     */
    public BizException(int code, String message, Throwable cause) {
        super(message, cause);
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
}
