package ext.library.metrics.alert;

import java.time.Instant;

/**
 * 告警事件
 *
 * @param metric 指标名，如「错误率」「P99 延迟」
 * @param target 对象，{@code global} 或端点名
 * @param detail 具体数值，如 {@code 3.42% > 1.00%}
 * @param at     触发时间
 */
public record AlertEvent(String metric, String target, String detail, Instant at) {
}
