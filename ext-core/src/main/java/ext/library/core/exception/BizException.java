package ext.library.core.exception;


import ext.library.core.response.ResponseCode;
import lombok.Getter;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

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
    public BizException(@NotNull ResponseCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    /**
     * 异常提示消息构造
     *
     * @param resultCode 结果代码
     * @param message    消息
     */
    public BizException(@NotNull ResponseCode resultCode, @NotNull @Nls String message) {
        super(message);
        this.code = resultCode.getCode();
    }

}
