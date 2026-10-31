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
    @DisplayName("默认构造：HTTP 200 + 业务码 0")
    void defaultConstructor() {
        R<Void> r = new R<>();
        assertEquals(HttpStatus.OK.value(), r.getStatus());
        assertEquals(R.SUCCESS_CODE, r.getCode());
        assertEquals(HttpStatus.OK.getReasonPhrase(), r.getMsg());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("ok(data)：带回业务数据")
    void okWithData() {
        R<String> r = R.ok("hello");
        assertEquals(HttpStatus.OK.value(), r.getStatus());
        assertEquals(R.SUCCESS_CODE, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    @DisplayName("ok()：data 为空")
    void okWithoutData() {
        R<Void> r = R.ok();
        assertEquals(HttpStatus.OK.value(), r.getStatus());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("failed(HttpStatus)：HTTP 状态与业务码分开")
    void failedWithStatus() {
        R<Void> r = R.failed(HttpStatus.UNAUTHORIZED);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), r.getStatus());
        assertEquals(BizCode.UNAUTHORIZED.getCode(), r.getCode());
        assertEquals(HttpStatus.UNAUTHORIZED.getReasonPhrase(), r.getMsg());
    }

    @Test
    @DisplayName("failed(ResponseCode)：HTTP 200 + 业务码")
    void failedWithResponseCode() {
        R<Void> r = R.failed(BizCode.UNKNOWN);
        assertEquals(HttpStatus.OK.value(), r.getStatus());
        assertEquals(BizCode.UNKNOWN.getCode(), r.getCode());
        assertEquals(BizCode.UNKNOWN.getMsg(), r.getMsg());
    }

    @Test
    @DisplayName("failed(int, String)：只设业务码，HTTP 状态保持 200")
    void failedWithCodeAndMessage() {
        R<Void> r = R.failed(600, "自定义消息");
        assertEquals(HttpStatus.OK.value(), r.getStatus());
        assertEquals(600, r.getCode());
        assertEquals("自定义消息", r.getMsg());
    }

}
