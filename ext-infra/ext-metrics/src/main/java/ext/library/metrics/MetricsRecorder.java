package ext.library.metrics;

/**
 * 指标埋点入口
 * <p>
 * 其它模块只依赖这个接口：{@code ext-resilience} 记限流拒绝、{@code ext-idempotent} 记幂等命中、
 * {@code ext-jdbc} 记慢 SQL、{@code ext-mvc} 记错误码分布。没有实现 Bean 时各模块按 {@code ObjectProvider}
 * 静默跳过，不因为没引监控就启动失败。
 */
public interface MetricsRecorder {

    /**
     * 记录一次 HTTP 请求
     *
     * @param endpoint 端点，建议用路径模板 {@code /user/{id}}，用原始 URI 会把内存打爆
     * @param status   响应状态码，{@code >= 500} 计入错误
     * @param millis   耗时
     */
    void http(String endpoint, int status, long millis);

    /**
     * 记录一次业务错误码
     *
     * @param code 响应体里的 {@code code}
     */
    void errorCode(String code);

    /**
     * 记录一次限流拒绝
     *
     * @param name 限流名称
     */
    void rateLimitRejected(String name);

    /**
     * 记录一次幂等命中
     *
     * @param name 接口标识
     */
    void idempotentHit(String name);

    /**
     * 记录一次 SQL 耗时，未达慢 SQL 阈值时由实现自行丢弃
     *
     * @param sql    SQL 文本，实现负责抹掉字面量
     * @param millis 耗时
     */
    void slowSql(String sql, long millis);
}
