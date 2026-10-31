package ext.library.web.handler;

import ext.library.servlet.response.R;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("全局异常处理")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ResponseStatusException 保留自身 HTTP 状态")
    void keepsStatusFromResponseStatusException() {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "订单不存在");

        ResponseEntity<R<Void>> response = handler.responseStatusException(exception, new MockHttpServletRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

}
