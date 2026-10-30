package ext.library.servlet.exception;

import ext.library.servlet.response.R;
import ext.library.tool.exception.ResponseCode;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 错误信封
 * <p>
 * 「异常 → (HTTP 状态, {@link R})」的组装结果。各模块的异常处理器只负责判定
 * <b>哪种异常归自己管</b>与该异常对外要说什么，信封的形状、HTTP 状态与响应体的对应关系
 * 统一由这里给出，避免每个 advice 各写一遍。
 * <p>
 * 业务码（如 {@code 6xx}）不是合法的 HTTP 状态码，所以 {@link #status()} 允许为 {@code null}：
 * 为空表示"HTTP 状态保持 200，错误信息只在响应体里"。
 *
 * @param status HTTP 状态，{@code null} 表示不改动 HTTP 状态
 * @param body   响应体
 * @since 4.1.0
 */
public record ErrorEnvelope(@Nullable HttpStatus status, R<Void> body) {

    /**
     * 按 HTTP 状态生成信封（消息取状态码的 reason phrase）
     *
     * @param status HTTP 状态
     *
     * @return 错误信封
     */
    public static ErrorEnvelope of(HttpStatus status) {
        return new ErrorEnvelope(status, R.failed(status));
    }

    /**
     * 按 HTTP 状态与自定义消息生成信封
     *
     * @param status  HTTP 状态
     * @param message 对外消息
     *
     * @return 错误信封
     */
    public static ErrorEnvelope of(HttpStatus status, String message) {
        return new ErrorEnvelope(status, R.failed(status, message));
    }

    /**
     * 按 HTTP 状态与业务码生成信封
     * <p>
     * 用于「HTTP 状态与业务码不是一回事」的场景，如内部错误：HTTP 500，业务码仍是
     * 统一的未知错误码。
     *
     * @param status  HTTP 状态
     * @param code    业务码
     * @param message 对外消息
     *
     * @return 错误信封
     */
    public static ErrorEnvelope of(HttpStatus status, int code, String message) {
        return new ErrorEnvelope(status, R.failed(code, message));
    }

    /**
     * 按业务码生成信封，HTTP 状态保持 200
     *
     * @param code    业务码
     * @param message 对外消息
     *
     * @return 错误信封
     */
    public static ErrorEnvelope ofCode(int code, String message) {
        return new ErrorEnvelope(null, R.failed(code, message));
    }

    /**
     * 按响应码生成信封，HTTP 状态保持 200
     *
     * @param responseCode 响应码
     *
     * @return 错误信封
     */
    public static ErrorEnvelope of(ResponseCode responseCode) {
        return ofCode(responseCode.getCode(), responseCode.getMsg());
    }

    /**
     * 转为可直接作为控制器返回值的响应实体
     *
     * @return 响应实体，{@code status} 为空时状态为 200
     */
    public ResponseEntity<R<Void>> toResponseEntity() {
        return status == null
                ? ResponseEntity.ok(body)
                : ResponseEntity.status(status).body(body);
    }

    /**
     * 是否显式指定了 HTTP 状态
     *
     * @return 指定了状态返回 {@code true}
     */
    public boolean hasStatus() {
        return status != null;
    }

}
