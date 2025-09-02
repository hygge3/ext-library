package ext.library.web.response;

import ext.library.tool.biz.exception.BizCode;
import ext.library.tool.biz.response.ResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * HTTP 请求最外层响应对象，更适应 RESTful 风格 API
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return ok(data, BizCode.SUCCESS.getMsg());
    }

    public static <T> R<T> ok(T data, String message) {
        return new R<T>().setCode(BizCode.SUCCESS.getCode()).setData(data).setMsg(message);
    }

    public static <T> R<T> failed(int code, String message) {
        return new R<T>().setCode(code).setMsg(message);
    }

    public static <T> R<T> failed(ResponseCode failMsg) {
        return failed(failMsg.getCode(), failMsg.getMsg());
    }

    public static <T> R<T> failed(ResponseCode failMsg, String message) {
        return failed(failMsg.getCode(), message);
    }

}