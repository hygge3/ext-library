package ext.library.servlet.exception;

import ext.library.servlet.response.R;
import ext.library.tool.exception.BizCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 错误信封测试
 */
@DisplayName("错误信封测试")
class ErrorEnvelopeTest {

    @Test
    @DisplayName("of(HttpStatus)：消息取 reason phrase")
    void ofStatus() {
        ErrorEnvelope envelope = ErrorEnvelope.of(HttpStatus.BAD_REQUEST);
        assertEquals(HttpStatus.BAD_REQUEST, envelope.status());
        assertTrue(envelope.hasStatus());
        assertEquals(400, envelope.body().getCode());
    }

    @Test
    @DisplayName("of(HttpStatus, message)：自定义消息")
    void ofStatusWithMessage() {
        ErrorEnvelope envelope = ErrorEnvelope.of(HttpStatus.FORBIDDEN, "没有权限");
        assertEquals("没有权限", envelope.body().getMsg());
        assertEquals(403, envelope.body().getCode());
    }

    @Test
    @DisplayName("ofCode：业务码保持 HTTP 200")
    void ofCode() {
        ErrorEnvelope envelope = ErrorEnvelope.ofCode(601, "业务失败");
        assertNull(envelope.status());
        assertFalse(envelope.hasStatus());

        ResponseEntity<R<Void>> entity = envelope.toResponseEntity();
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        assertEquals(601, entity.getBody().getCode());
    }

    @Test
    @DisplayName("of(ResponseCode)：取业务码与业务消息")
    void ofResponseCode() {
        ErrorEnvelope envelope = ErrorEnvelope.of(BizCode.UNKNOWN);
        assertEquals(BizCode.UNKNOWN.getCode(), envelope.body().getCode());
        assertEquals(BizCode.UNKNOWN.getMsg(), envelope.body().getMsg());
    }

    @Test
    @DisplayName("toResponseEntity：HTTP 状态落到响应实体")
    void toResponseEntityWithStatus() {
        ResponseEntity<R<Void>> entity = ErrorEnvelope.of(HttpStatus.UNAUTHORIZED).toResponseEntity();
        assertEquals(401, entity.getStatusCode().value());
        assertEquals(401, entity.getBody().getCode());
    }

}
