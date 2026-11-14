package ext.library.metrics.core;

import java.util.List;

/**
 * 指标快照
 * <p>
 * 一次取全量，数字都来自同一窗口，不会出现「错误率算完了但请求数又涨了」的抖动。
 */
public record MetricsSnapshot(long windowSeconds,
                              Rates rates,
                              List<Endpoint> endpoints,
                              List<Count> errorCodes,
                              List<Count> rateLimitRejected,
                              List<Count> idempotentHits,
                              List<SlowSql> slowSql,
                              List<Pool> pools) {

    /**
     * 全局汇总
     *
     * @param requests   窗口内请求数
     * @param errors     窗口内 5xx 数
     * @param qps        按整窗口归一的每秒请求数
     * @param errorRate  5xx 占比，{@code 0.012} 即 1.2%
     * @param p99Millis  P99 延迟（桶上界）
     */
    public record Rates(long requests, long errors, double qps, double errorRate, long p99Millis) {
    }

    /**
     * 单端点指标
     */
    public record Endpoint(String name, long requests, long errors, double qps, double errorRate, long p99Millis) {
    }

    /**
     * 命名计数
     */
    public record Count(String name, long count) {
    }

    /**
     * 慢 SQL 记录
     * <p>
     * SQL 已抹掉字面量（只留 {@code ?} 与标识符），避免把用户数据带进日志与接口。
     */
    public record SlowSql(String sql, long millis, String traceId) {
    }

    /**
     * 池水位
     *
     * @param active        活跃数：线程池是正在跑的任务数，连接池是借出的连接数
     * @param size          当前大小：线程池是工作线程数，连接池是已建连接数
     * @param maxSize       上限：线程池是最大线程数，连接池是最大连接数
     * @param queueSize     队列中等待数，连接池无队列时为 0
     * @param queueCapacity 队列容量，连接池无队列时为 0
     */
    public record Pool(String name, String type, int active, int size, int maxSize, int queueSize, int queueCapacity) {
    }
}
