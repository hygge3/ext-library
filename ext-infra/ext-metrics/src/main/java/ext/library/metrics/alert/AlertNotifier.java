package ext.library.metrics.alert;

/**
 * 告警出口
 * <p>
 * 默认实现只打 WARN 日志。要发邮件、Webhook、钉钉，提供一个同名 Bean 即可，模块不内置任何网络调用——
 * 通知渠道的超时、重试、鉴权各项目都不一样，塞进库里只会被绕开。
 */
@FunctionalInterface
public interface AlertNotifier {

    /**
     * 发送告警
     * <p>
     * 实现抛异常不会影响评估器继续工作，异常只被记为 WARN。
     *
     * @param event 告警事件
     */
    void notify(AlertEvent event);
}
