package ext.library.metrics.alert;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;

/**
 * 默认告警出口：WARN 日志
 * <p>
 * 日志里带指标名与对象，配合 traceId 能直接定位到那一批请求。
 */
public class LoggingAlertNotifier implements AlertNotifier {

    @Override
    public void notify(AlertEvent event) {
        Logs.warn(EmojiSymbol.METRICS, "指标告警：{} {} → {}", event.metric(), event.target(), event.detail());
    }
}
