package ext.library.servlet.response;

import ext.library.tool.exception.BizCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * R 统一响应体测试
 */
@DisplayName("R 统一响应体测试")
class RTest {

    @Test
    @DisplayName("默认构造：200 + OK")
    void defaultConstructor() {
        R<Void> r = new R<>();
        assertEquals(HttpStatus.OK.value(), r.getCode());
        assertEquals(HttpStatus.OK.getReasonPhrase(), r.getMsg());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("ok(data)：带回业务数据")
    void okWithData() {
        R<String> r = R.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    @DisplayName("ok()：data 为空")
    void okWithoutData() {
        R<Void> r = R.ok();
        assertEquals(200, r.getCode());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("failed(HttpStatus)：状态码与 reason phrase")
    void failedWithStatus() {
        R<Void> r = R.failed(HttpStatus.UNAUTHORIZED);
        assertEquals(401, r.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), r.getMsg());
    }

    @Test
    @DisplayName("failed(ResponseCode)：取业务码与业务消息")
    void failedWithResponseCode() {
        R<Void> r = R.failed(BizCode.UNKNOWN);
        assertEquals(BizCode.UNKNOWN.getCode(), r.getCode());
        assertEquals(BizCode.UNKNOWN.getMsg(), r.getMsg());
    }

    @Test
    @DisplayName("failed(int, String)：业务码不进 HTTP 状态")
    void failedWithCodeAndMessage() {
        R<Void> r = R.failed(600, "自定义消息");
        assertEquals(600, r.getCode());
        assertEquals("自定义消息", r.getMsg());
    }

}
