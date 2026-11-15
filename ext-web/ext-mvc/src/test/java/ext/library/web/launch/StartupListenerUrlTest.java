package ext.library.web.launch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 启动信息里的 URL 拼接：不能出现 {@code http://host:port//v3/api-docs} 这种双斜杠
 */
@DisplayName("启动信息 URL 拼接测试")
class StartupListenerUrlTest {

    @Test
    void trimsTrailingSlash() {
        assertEquals("", StartupListener.trimTrailingSlash("/"));
        assertEquals("/app", StartupListener.trimTrailingSlash("/app/"));
        assertEquals("/app", StartupListener.trimTrailingSlash("/app"));
        assertEquals("", StartupListener.trimTrailingSlash(""));
    }

    @Test
    void leadsWithSlash() {
        assertEquals("/v3/api-docs", StartupListener.leadWithSlash("/v3/api-docs"));
        assertEquals("/v3/api-docs", StartupListener.leadWithSlash("v3/api-docs"));
    }

    @Test
    void composesWithoutDoubleSlash() {
        // 默认 context-path 为 "/"，去掉后拼出来是 http://host:8080/v3/api-docs
        String url = "http://host:8080" + StartupListener.trimTrailingSlash("/");
        assertEquals("http://host:8080" + StartupListener.leadWithSlash("/v3/api-docs"),
                url + StartupListener.leadWithSlash("/v3/api-docs"));
    }
}
