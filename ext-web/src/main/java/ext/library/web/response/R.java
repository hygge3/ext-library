package ext.library.web.response;

import ext.library.tool.biz.exception.BizCode;
import ext.library.tool.biz.response.ResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

/**
 * HTTP 请求最外层响应对象，更适应 RESTful 风格 API
 */
@Schema(title = "返回体结构")
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    @Schema(title = "返回状态码", defaultValue = "200")
    private int code;

    /**
     * 响应提示
     */
    @Schema(title = "返回信息", defaultValue = "Success")
    private String msg;

    /**
     * 业务数据
     */
    @Schema(title = "数据", nullable = true, defaultValue = "null")
    private T data;

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public R() {
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return ok(data, BizCode.SUCCESS.getMsg());
    }

    public static <T> R<T> ok(T data, String message) {
        return new R<>(BizCode.SUCCESS.getCode(), message, data);
    }

    public static <T> R<T> failed(int code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> failed(ResponseCode failMsg) {
        return failed(failMsg.getCode(), failMsg.getMsg());
    }

    public static <T> R<T> failed(ResponseCode failMsg, String message) {
        return failed(failMsg.getCode(), message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "R{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}