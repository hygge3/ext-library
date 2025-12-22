package ext.library.tool.exception;


/**
 * 业务异常
 */
public class BizException extends RuntimeException {

    private String code = BizCode.WARN.getCode();

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(ResponseCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
