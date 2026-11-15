package ext.library.tool.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.BizException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Logs 格式化测试：模块名占头，业务占位符照常替换
 */
@DisplayName("Logs 日志格式化测试")
class LogsTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(LogsTest.class);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void attach() {
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detach() {
        logger.detachAppender(appender);
    }

    @Test
    void rendersModuleAndArguments() {
        Logs.info(EmojiSymbol.METRICS, "指标大屏：{}", "/metrics");

        assertEquals(EmojiSymbol.METRICS + "|指标大屏：/metrics",
                appender.list.getFirst().getFormattedMessage());
    }

    @Test
    void knownExceptionDropsStack() {
        // 未登录这类已知异常：降 WARN，只留类型与消息
        Logs.error(EmojiSymbol.SECURITY, new BizException(401, "未登录认证"), "URI:{},{}", "/notes", "拒绝");

        ILoggingEvent event = appender.list.getFirst();
        assertEquals(Level.WARN, event.getLevel());
        assertNull(event.getThrowableProxy());
        assertEquals("🛡️|URI:/notes,拒绝|ext.library.tool.exception.BizException: 未登录认证",
                event.getFormattedMessage());
    }

    @Test
    void bizWrapperOfRealFailureKeepsStack() {
        // 邮件/数据库这类真实故障即使被包成业务异常，也要留堆栈
        Logs.error(EmojiSymbol.MAIL, new BizException("邮件发送失败", new java.net.ConnectException("Connection refused")), "URI:{}", "/api/auth/code");

        ILoggingEvent event = appender.list.getFirst();
        assertEquals(Level.ERROR, event.getLevel());
        assertNotNull(event.getThrowableProxy());
    }

    @Test
    void unexpectedExceptionKeepsStack() {
        Logs.error(EmojiSymbol.SECURITY, new IllegalStateException("boom"), "URI:{}", "/notes");

        ILoggingEvent event = appender.list.getFirst();
        assertEquals(Level.ERROR, event.getLevel());
        assertNotNull(event.getThrowableProxy());
        assertEquals("🛡️|URI:/notes", event.getFormattedMessage());
    }

    @Test
    void keepsThrowableOutOfMessage() {
        RuntimeException error = new RuntimeException("boom");

        Logs.warn(EmojiSymbol.METRICS, error, "失败：{}", 1);

        ILoggingEvent event = appender.list.getFirst();
        assertEquals(EmojiSymbol.METRICS + "|失败：1", event.getFormattedMessage());
        assertEquals("boom", event.getThrowableProxy().getMessage());
    }
}
