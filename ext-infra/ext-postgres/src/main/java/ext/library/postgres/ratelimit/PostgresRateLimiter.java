package ext.library.postgres.ratelimit;

import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * PostgreSQL 限流器
 * <p>
 * 使用固定窗口算法实现限流，支持原子性检查和递增
 *
 * @since 4.0.0
 */
public class PostgresRateLimiter {

    private final JdbcClient jdbcClient;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresRateLimiter(JdbcClient jdbcClient, PostgresProperties properties) {
        this.jdbcClient = jdbcClient;
        this.properties = properties;
        this.tableName = properties.getRateLimitTableName();
    }

    /**
     * 检查并递增（使用默认配置）
     *
     * @param key 限流 key（如 user:123, ip:192.168.1.1）
     *
     * @return 限流结果
     */
    public RateLimitResult checkAndIncrement(String key) {
        return checkAndIncrement(key, properties.getDefaultRateLimit(), properties.getRateLimitWindow());
    }

    /**
     * 检查并递增
     *
     * @param key    限流 key
     * @param limit  限制阈值
     * @param window 时间窗口
     *
     * @return 限流结果
     */
    public RateLimitResult checkAndIncrement(String key, int limit, Duration window) {
        String interval = window.toSeconds() + " seconds";

        return jdbcClient.sql("""
                        INSERT INTO %s (key, request_count, window_start)
                        VALUES (?, 1, NOW())
                        ON CONFLICT (key) DO UPDATE SET
                            request_count = CASE
                                WHEN %s.window_start < NOW() - ?::interval THEN 1
                                ELSE %s.request_count + 1
                            END,
                            window_start = CASE
                                WHEN %s.window_start < NOW() - ?::interval THEN NOW()
                                ELSE %s.window_start
                            END
                        RETURNING request_count, window_start
                        """.formatted(tableName, tableName, tableName, tableName, tableName)).param(key).param(interval).param(interval).query((rs, _) -> {
                    int count = rs.getInt("request_count");
                    Timestamp windowStart = rs.getTimestamp("window_start");
                    boolean allowed = count <= limit;
                    int remaining = Math.max(0, limit - count);
                    long resetAt = windowStart.toInstant().plus(window).toEpochMilli();

                    RateLimitResult result = new RateLimitResult(allowed, count, remaining, limit, resetAt);

                    if (!allowed) {
                        Logs.debug(EmojiSymbol.POSTGRES, "请求被限流：key={}, count={}, limit={}", key, count, limit);
                    }

                    return result;
                }).optional()
                // 默认允许（查询失败时的保守策略）
                .orElseGet(() -> new RateLimitResult(true, 0, limit, limit, System.currentTimeMillis() + window.toMillis()));
    }

    /**
     * 仅检查是否允许（不递增计数）
     *
     * @param key 限流 key
     *
     * @return 是否允许
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, properties.getDefaultRateLimit(), properties.getRateLimitWindow());
    }

    /**
     * 仅检查是否允许（不递增计数）
     *
     * @param key    限流 key
     * @param limit  限制阈值
     * @param window 时间窗口
     *
     * @return 是否允许
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        return jdbcClient.sql("""
                SELECT request_count FROM %s
                WHERE key = ? AND window_start > NOW() - ?::interval
                """.formatted(tableName)).param(key).param(window.toSeconds() + " seconds").query(Integer.class).optional().map(count -> count < limit).orElse(true); // 不存在记录，允许请求
    }

    /**
     * 获取当前计数
     *
     * @param key 限流 key
     *
     * @return 当前计数，不存在返回 0
     */
    public int getCurrentCount(String key) {
        return jdbcClient.sql("""
                SELECT request_count FROM %s
                WHERE key = ? AND window_start > NOW() - ?::interval
                """.formatted(tableName)).param(key).param(properties.getRateLimitWindow().toSeconds() + " seconds").query(Integer.class).optional().orElse(0);
    }

    /**
     * 重置计数
     *
     * @param key 限流 key
     *
     * @return 是否重置成功
     */
    public boolean reset(String key) {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE key = ?").param(key).update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "重置限流计数：key={}", key);
        }
        return deleted > 0;
    }

    /**
     * 按模式重置计数
     *
     * @param pattern 匹配模式（支持 * 通配符）
     *
     * @return 重置的记录数
     */
    public int resetByPattern(String pattern) {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE key LIKE ?").param(pattern.replace("*", "%")).update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "按模式重置限流计数：pattern={}, count={}", pattern, deleted);
        }
        return deleted;
    }

    /**
     * 定时清理过期的限流记录
     */
    @Scheduled(fixedDelayString = "#{@postgresProperties.rateLimitWindow.toMillis() * 2}")
    public void cleanup() {
        // 清理超过两个窗口周期的旧记录
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE window_start < NOW() - ?::interval").param((properties.getRateLimitWindow().toSeconds() * 2) + " seconds").update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理过期限流记录：{} 条", deleted);
        }
    }

    /**
     * 获取统计信息
     *
     * @return 当前有效的限流记录数
     */
    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE window_start > NOW() - ?::interval").param(properties.getRateLimitWindow().toSeconds() + " seconds").query(Long.class).single();
    }
}
