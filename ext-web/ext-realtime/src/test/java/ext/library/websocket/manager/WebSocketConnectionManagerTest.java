package ext.library.websocket.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("WebSocket 连接管理")
class WebSocketConnectionManagerTest {

    private final WebSocketConnectionManager connectionManager = new WebSocketConnectionManager();

    private static WebSocketSession session(String sessionId) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(sessionId);
        return session;
    }

    @Test
    @DisplayName("同一用户多标签页共存，关掉一个不影响另一个")
    void keepsSessionsOfSameUser() {
        connectionManager.addSession("u1", session("s1"));
        connectionManager.addSession("u1", session("s2"));
        assertThat(connectionManager.getSessions("u1")).hasSize(2);

        connectionManager.removeSession("u1", "s1");
        assertThat(connectionManager.hasSession("u1")).isTrue();
        assertThat(connectionManager.getSessions("u1")).hasSize(1);

        connectionManager.removeSession("u1", "s2");
        assertThat(connectionManager.hasSession("u1")).isFalse();
    }

}
