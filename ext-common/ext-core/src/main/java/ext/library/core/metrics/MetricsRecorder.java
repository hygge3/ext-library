package ext.library.core.metrics;

/**
 * 指标记录点
 * <p>
 * 只声明契约，由 ext-metrics 实现。没引入 ext-metrics 时容器里没有实现，业务模块
 * 用 {@code ObjectProvider<MetricsRecorder>} 取，取不到就当埋点不存在。
 */
public interface MetricsRecorder {

    /**
     * 限流拒绝一次
     *
     * @param name 限流桶标识
     */
    void rejected(String name);

    /**
     * 幂等命中一次（重复请求被拦下）
     *
     * @param name 幂等键方法标识
     */
    void deduplicated(String name);

    /**
     * 业务错误码出现一次
     *
     * @param code 错误码
     */
    void businessError(String code);

    /**
     * 慢查询一次
     *
     * @param sql    原始 SQL，脱敏由实现负责
     * @param millis 耗时
     */
    void slowQuery(String sql, long millis);
}
