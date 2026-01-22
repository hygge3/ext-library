package ext.library.postgres.ratelimit;

import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final DataSource dataSource;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresRateLimiter(DataSource dataSource, PostgresProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.tableName = properties.getRateLimitTableName();
    }

    /**
     * 检查并递增（使用默认配置）
     *
     * @param key 限流 key（如 user:123, ip:192.168.1.1）
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
     * @return 限流结果
     */
    public RateLimitResult checkAndIncrement(String key, int limit, Duration window) {
        String sql = """
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
                """.formatted(tableName, tableName, tableName, tableName, tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String interval = window.toSeconds() + " seconds";
            ps.setString(1, key);
            ps.setString(2, interval);
            ps.setString(3, interval);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("request_count");
                    Timestamp windowStart = rs.getTimestamp("window_start");
                    boolean allowed = count <= limit;
                    int remaining = Math.max(0, limit - count);
                    long resetAt = windowStart.toInstant().plus(window).toEpochMilli();

                    RateLimitResult result = new RateLimitResult(allowed, count, remaining, limit, resetAt);

                    if (!allowed) {
                        Logs.debug(EmojiSymbol.POSTGRES, "请求被限流: key={}, count={}, limit={}", key, count, limit);
                    }

                    return result;
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "限流检查失败: key={}", key);
            throw new RuntimeException("Failed to check rate limit for: " + key, e);
        }

        // 默认允许（查询失败时的保守策略）
        return new RateLimitResult(true, 0, limit, limit, System.currentTimeMillis() + window.toMillis());
    }

    /**
     * 仅检查是否允许（不递增计数）
     *
     * @param key 限流 key
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
     * @return 是否允许
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        String sql = """
                SELECT request_count FROM %s
                WHERE key = ? AND window_start > NOW() - ?::interval
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, window.toSeconds() + " seconds");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("request_count") < limit;
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "限流检查失败: key={}", key);
            throw new RuntimeException("Failed to check rate limit for: " + key, e);
        }
        return true; // 不存在记录，允许请求
    }

    /**
     * 获取当前计数
     *
     * @param key 限流 key
     * @return 当前计数，不存在返回 0
     */
    public int getCurrentCount(String key) {
        String sql = """
                SELECT request_count FROM %s
                WHERE key = ? AND window_start > NOW() - ?::interval
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, properties.getRateLimitWindow().toSeconds() + " seconds");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("request_count");
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取限流计数失败: key={}", key);
            throw new RuntimeException("Failed to get rate limit count for: " + key, e);
        }
        return 0;
    }

    /**
     * 重置计数
     *
     * @param key 限流 key
     * @return 是否重置成功
     */
    public boolean reset(String key) {
        String sql = "DELETE FROM " + tableName + " WHERE key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                Logs.debug(EmojiSymbol.POSTGRES, "重置限流计数: key={}", key);
            }
            return deleted;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "重置限流计数失败: key={}", key);
            throw new RuntimeException("Failed to reset rate limit for: " + key, e);
        }
    }

    /**
     * 按模式重置计数
     *
     * @param pattern 匹配模式（支持 * 通配符）
     * @return 重置的记录数
     */
    public int resetByPattern(String pattern) {
        String sql = "DELETE FROM " + tableName + " WHERE key LIKE ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern.replace("*", "%"));
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "按模式重置限流计数: pattern={}, count={}", pattern, deleted);
            }
            return deleted;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "按模式重置限流计数失败: pattern={}", pattern);
            throw new RuntimeException("Failed to reset rate limit by pattern: " + pattern, e);
        }
    }

    /**
     * 定时清理过期的限流记录
     */
    @Scheduled(fixedDelayString = "#{@postgresProperties.rateLimitWindow.toMillis() * 2}")
    public void cleanup() {
        // 清理超过两个窗口周期的旧记录
        String sql = "DELETE FROM " + tableName + " WHERE window_start < NOW() - ?::interval";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (properties.getRateLimitWindow().toSeconds() * 2) + " seconds");
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "清理过期限流记录: {} 条", deleted);
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "清理过期限流记录失败");
        }
    }

    /**
     * 获取统计信息
     *
     * @return 当前有效的限流记录数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE window_start > NOW() - ?::interval";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, properties.getRateLimitWindow().toSeconds() + " seconds");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取限流统计失败");
            throw new RuntimeException("Failed to get rate limit count", e);
        }
    }
}
