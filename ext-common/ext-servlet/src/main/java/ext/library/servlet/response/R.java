package ext.library.servlet.response;

import ext.library.tool.exception.BizCode;
import ext.library.tool.exception.ResponseCode;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一响应体
 * <p>
 * HTTP 请求最外层响应对象，适应 RESTful 风格 API。
 * <p>
 * 字段职责划分：
 * <ul>
 *   <li>{@code status}：HTTP 状态码（如 200 / 400 / 404 / 500），反映请求的处理结果</li>
 *   <li>{@code code}：业务码（如 {@link BizCode} 的 4 位码），{@link #SUCCESS_CODE} 表示成功</li>
 *   <li>{@code msg}：提示消息</li>
 *   <li>{@code data}：业务数据</li>
 * </ul>
 * 这样前端可同时拿到「HTTP 层」与「业务层」两套语义，互不干扰。
 *
 * @param <T> 业务数据类型
 *
 * @since 2025.01.01
 */
public class R<T> implements Serializable {

    /** 成功业务码 */
    public static final int SUCCESS_CODE = 0;

    @Serial
    private static final long serialVersionUID = 1L;

    /** HTTP 状态码 */
    private int status = HttpStatus.OK.value();

    /** 业务码，{@link #SUCCESS_CODE} 表示成功 */
    private int code = SUCCESS_CODE;

    /** 提示消息 */
    private String msg = HttpStatus.OK.getReasonPhrase();

    /** 业务数据 */
    private T data;

    public R() {
    }

    public R(int status, int code, String msg, T data) {
        this.status = status;
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 获取 HTTP 状态码
     *
     * @return HTTP 状态码
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置 HTTP 状态码
     *
     * @param status HTTP 状态码
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 获取业务码
     *
     * @return 业务码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置业务码
     *
     * @param code 业务码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取提示消息
     *
     * @return 提示消息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置提示消息
     *
     * @param msg 提示消息
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
     *
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
     *
     * @return 成功响应
     */
    public static <T> R<T> ok(T data) {
        return new R<>(HttpStatus.OK.value(), SUCCESS_CODE, HttpStatus.OK.getReasonPhrase(), data);
    }

    /**
     * 失败响应（业务异常，HTTP 状态保持 200，以业务码区分错误）
     *
     * @param responseCode 响应码（提供业务码和消息）
     * @param <T>          数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(ResponseCode responseCode) {
        return failed(responseCode, responseCode.getMsg());
    }

    /**
     * 失败响应（业务异常，HTTP 状态保持 200，自定义消息）
     *
     * @param responseCode 响应码（提供业务码）
     * @param message      自定义消息
     * @param <T>          数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(ResponseCode responseCode, String message) {
        return failed(responseCode.getCode(), message);
    }

    /**
     * 失败响应（业务码 + 消息，HTTP 状态保持 200）
     *
     * @param code    业务码
     * @param message 消息
     * @param <T>     数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(int code, String message) {
        return new R<>(HttpStatus.OK.value(), code, message, null);
    }

    /**
     * 失败响应（按 HTTP 状态，业务码自动按状态映射）
     *
     * @param status HTTP 状态
     * @param <T>    数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(HttpStatus status) {
        return failed(status, status.getReasonPhrase());
    }

    /**
     * 失败响应（按 HTTP 状态 + 自定义消息，业务码自动按状态映射）
     *
     * @param status  HTTP 状态
     * @param message 自定义消息
     * @param <T>     数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(HttpStatus status, String message) {
        return new R<>(status.value(), defaultCode(status).getCode(), message, null);
    }

    /**
     * 失败响应（按 HTTP 状态 + 显式业务码，消息取响应码默认消息）
     *
     * @param status       HTTP 状态
     * @param responseCode 响应码（提供业务码和消息）
     * @param <T>          数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(HttpStatus status, ResponseCode responseCode) {
        return failed(status, responseCode, responseCode.getMsg());
    }

    /**
     * 失败响应（按 HTTP 状态 + 显式业务码 + 自定义消息）
     *
     * @param status       HTTP 状态
     * @param responseCode 响应码（提供业务码）
     * @param message      自定义消息
     * @param <T>          数据类型
     *
     * @return 失败响应
     */
    public static <T> R<T> failed(HttpStatus status, ResponseCode responseCode, String message) {
        return new R<>(status.value(), responseCode.getCode(), message, null);
    }

    /**
     * 按 HTTP 状态映射默认业务码
     *
     * @param status HTTP 状态
     *
     * @return 默认业务码
     */
    private static BizCode defaultCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> BizCode.INVALID_PARAMETER;
            case UNAUTHORIZED -> BizCode.UNAUTHORIZED;
            case FORBIDDEN -> BizCode.FORBIDDEN;
            case NOT_FOUND -> BizCode.NOT_FOUND;
            case CONFLICT -> BizCode.DATA_CONFLICT;
            case TOO_MANY_REQUESTS -> BizCode.RATE_LIMITED;
            default -> status.is5xxServerError() ? BizCode.UNKNOWN : BizCode.INVALID_PARAMETER;
        };
    }

    @Override
    public String toString() {
        return "R{" +
                "status=" + status +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
