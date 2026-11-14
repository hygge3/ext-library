package ext.library.servlet.response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("统一响应体")
class RTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    @DisplayName("traceId 取自 MDC，无上下文时为 null")
    void fillsTraceIdFromMdc() {
        MDC.put("traceId", "a1b2c3d4");
        assertThat(R.ok().getTraceId()).isEqualTo("a1b2c3d4");

        MDC.clear();
        assertThat(R.failed(400, "参数错误").getTraceId()).isNull();
    }

    @Test
    @DisplayName("timestamp 为生成时刻")
    void fillsTimestamp() {
        long before = System.currentTimeMillis();
        long timestamp = R.ok("data").getTimestamp();

        assertThat(timestamp).isBetween(before, System.currentTimeMillis());
    }

}
