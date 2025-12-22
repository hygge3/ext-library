package ext.library.web.response;

import ext.library.tool.exception.ResponseCode;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 返回体结构
 * <p>
 * HTTP 请求最外层响应对象，更适应 RESTful 风格 API
 */
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    private String code = "200";

    /**
     * 响应提示
     */
    private String msg = "Ok";

    /**
     * 业务数据
     */
    private T data;

    public R() {
    }

    public R(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 获取响应状态码
     *
     * @return 响应状态码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置响应状态码
     *
     * @param code 响应状态码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取响应提示
     *
     * @return 响应提示
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置响应提示
     *
     * @param msg 响应提示
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * 获取业务数据
     *
     * @return 业务数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置业务数据
     *
     * @param data 业务数据
     */
    public void setData(T data) {
        this.data = data;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(String.valueOf(HttpStatus.OK.value()), HttpStatus.OK.getReasonPhrase(), data);
    }

    public static <T> R<T> failed(String code, String message) {
        return new R<>(code, message, null);
    }

    public static <T> R<T> failed(ResponseCode failMsg) {
        return failed(failMsg.getCode(), failMsg.getMsg());
    }

    public static <T> R<T> failed(HttpStatus status) {
        return failed(String.valueOf(status.value()), status.getReasonPhrase());
    }

    public static <T> R<T> failed(ResponseCode failMsg, String message) {
        return failed(failMsg.getCode(), message);
    }

    public static <T> R<T> failed(HttpStatus status, String message) {
        return failed(String.valueOf(status.value()), message);
    }

    @Override
    public String toString() {
        return "R{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
