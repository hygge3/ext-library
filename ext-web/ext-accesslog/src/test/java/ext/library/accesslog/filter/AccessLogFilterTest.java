package ext.library.accesslog.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 访问日志查询串掩码测试
 */
@DisplayName("访问日志查询串掩码")
class AccessLogFilterTest {

    @Test
    @DisplayName("敏感参数值被掩码，其余原样保留")
    void masksSensitiveParams() {
        assertThat(AccessLogFilter.maskQuery("page=2&access_token=abc123&name=zs"))
                .isEqualTo("page=2&access_token=***&name=zs");
    }

    @Test
    @DisplayName("参数名大小写不敏感")
    void masksCaseInsensitive() {
        assertThat(AccessLogFilter.maskQuery("Password=abc")).isEqualTo("Password=***");
    }

    @Test
    @DisplayName("无敏感参数与空串原样返回")
    void keepsPlainQuery() {
        assertThat(AccessLogFilter.maskQuery("page=2&size=10")).isEqualTo("page=2&size=10");
        assertThat(AccessLogFilter.maskQuery(null)).isNull();
        assertThat(AccessLogFilter.maskQuery("")).isEmpty();
    }

}
