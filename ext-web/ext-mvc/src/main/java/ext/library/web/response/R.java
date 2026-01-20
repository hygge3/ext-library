package ext.library.web.response;

import ext.library.tool.exception.ResponseCode;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应体
 * <p>
 * HTTP 请求最外层响应对象，适应 RESTful 风格 API。
 *
 * @param <T> 业务数据类型
 * @since 2025.01.01
 */
public class R<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 响应状态码 */
    private int code = HttpStatus.OK.value();

    /** 响应消息 */
    private String msg = HttpStatus.OK.getReasonPhrase();

    /** 业务数据 */
    private T data;

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 获取响应状态码
     *
     * @return 响应状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置响应状态码
     *
     * @param code 响应状态码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     *
     * @return 响应消息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置响应消息
     *
     * @param msg 响应消息
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

    /**
     * 成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     *
     * @param data 业务数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(HttpStatus.OK.value(), HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * 失败响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> failed(int code, String message) {
        return new R<>(code, message, null);
    }

    /**
     * 失败响应
     *
     * @param responseCode 响应码
     * @param <T>          数据类型
     * @return 失败响应
     */
    public static <T> R<T> failed(ResponseCode responseCode) {
        return failed(responseCode.getCode(), responseCode.getMsg());
    }

    /**
     * 失败响应
     *
     * @param status HTTP 状态
     * @param <T>    数据类型
     * @return 失败响应
     */
    public static <T> R<T> failed(HttpStatus status) {
        return failed(status.value(), status.getReasonPhrase());
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param responseCode 响应码
     * @param message      自定义消息
     * @param <T>          数据类型
     * @return 失败响应
     */
    public static <T> R<T> failed(ResponseCode responseCode, String message) {
        return failed(responseCode.getCode(), message);
    }

    /**
     * 失败响应（自定义消息）
     *
     * @param status  HTTP 状态
     * @param message 自定义消息
     * @param <T>     数据类型
     * @return 失败响应
     */
    public static <T> R<T> failed(HttpStatus status, String message) {
        return failed(status.value(), message);
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
