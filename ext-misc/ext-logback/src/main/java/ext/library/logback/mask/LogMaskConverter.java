package ext.library.logback.mask;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * 日志脱敏转换器
 * <p>
 * 在日志格式化阶段对消息正文逐条执行脱敏，规则来自 {@link LogMaskRegistry}。
 * <p>
 * 在 logback.xml 中启用：
 * <pre>{@code
 * <conversionRule conversionWord="maskedMsg" converterClass="ext.library.logback.mask.LogMaskConverter"/>
 *
 * <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
 *     <encoder>
 *         <pattern>%d{HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %maskedMsg%n</pattern>
 *     </encoder>
 * </appender>
 * }</pre>
 */
public class LogMaskConverter extends MessageConverter {

    @Override
    public String convert(ILoggingEvent event) {
        String message = super.convert(event);
        var rules = LogMaskRegistry.getRules();
        if (message == null || rules.isEmpty()) {
            return message;
        }
        for (LogMaskRule rule : rules) {
            message = rule.mask(message);
        }
        return message;
    }

}
