package ext.library.sse.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SSE 连接管理")
class SseConnectionManagerTest {

    private final SseConnectionManager connectionManager = new SseConnectionManager();

    @Test
    @DisplayName("最后一个连接断开后外层用户键也被清理")
    void removesUserKeyWhenNoConnectionLeft() {
        connectionManager.connect("u1", "t1");
        connectionManager.connect("u1", "t2");
        assertThat(connectionManager.hasConnection("u1")).isTrue();

        connectionManager.disconnect("u1", "t1");
        assertThat(connectionManager.hasConnection("u1")).isTrue();

        connectionManager.disconnect("u1", "t2");
        // 外层键残留会让 hasConnection 永远为 true，跨实例消息被误判为「本机有连接」而丢失
        assertThat(connectionManager.hasConnection("u1")).isFalse();
    }

}
