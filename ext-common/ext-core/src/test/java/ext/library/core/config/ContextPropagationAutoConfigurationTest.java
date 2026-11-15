package ext.library.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ext.library.core.context.ContextTaskDecorator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 上下文传播自动配置测试
 */
@DisplayName("上下文传播自动配置测试")
class ContextPropagationAutoConfigurationTest {

    private final Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ContextPropagationAutoConfiguration.class));

    @BeforeEach
    void attach() {
        appender.start();
        root.addAppender(appender);
    }

    @AfterEach
    void detach() {
        root.detachAppender(appender);
    }

    /** 配置类产生的 WARN 事件（覆盖装饰器的那条） */
    private List<String> overwriteWarnings() {
        return appender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .filter(event -> event.getLoggerName().startsWith(ContextPropagationAutoConfiguration.class.getName()))
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    @Test
    void postProcessesThreadPoolTaskExecutor() {
        runner.withUserConfiguration(ExecutorConfig.class).run(context -> {
            ThreadPoolTaskExecutor executor = context.getBean(ThreadPoolTaskExecutor.class);
            assertTrue(ReflectionTestUtils.getField(executor, "taskDecorator") instanceof ContextTaskDecorator,
                    "线程池未被挂上上下文传播装饰器");
            // 原先没有装饰器，不该打覆盖警告
            assertEquals(List.of(), overwriteWarnings());
        });
    }

    @Test
    void warnsOnlyWhenOverwriting() {
        runner.withUserConfiguration(DecoratedExecutorConfig.class).run(context -> {
            ThreadPoolTaskExecutor executor = context.getBean(ThreadPoolTaskExecutor.class);
            assertTrue(ReflectionTestUtils.getField(executor, "taskDecorator") instanceof ContextTaskDecorator);
            assertEquals(1, overwriteWarnings().size(), "覆盖了已有装饰器却未告警");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class ExecutorConfig {
        @Bean
        ThreadPoolTaskExecutor poolTaskExecutor() {
            return new ThreadPoolTaskExecutor();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class DecoratedExecutorConfig {
        @Bean
        ThreadPoolTaskExecutor poolTaskExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            executor.setTaskDecorator(runnable -> runnable);
            return executor;
        }
    }
}
