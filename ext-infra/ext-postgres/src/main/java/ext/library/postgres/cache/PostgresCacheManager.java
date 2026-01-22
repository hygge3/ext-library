package ext.library.postgres.cache;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

/**
 * PostgreSQL 缓存管理器
 * <p>
 * 使用 UNLOGGED 表实现高性能缓存，支持 TTL 过期和自动清理
 *
 * @since 4.0.0
 */
public class PostgresCacheManager {

    private final DataSource dataSource;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresCacheManager(DataSource dataSource, PostgresProperties properties) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.tableName = properties.getCacheTableName();
    }

    /**
     * 获取缓存值
     *
     * @param key   缓存 key
     * @param clazz 值类型
     * @return 缓存值，不存在或已过期返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        String sql = "SELECT value FROM " + tableName + " WHERE key = ? AND expires_at > NOW()";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("value");
                    return JsonUtil.readObj(json, clazz);
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取缓存失败: {}", key);
            throw new RuntimeException("Failed to get cache: " + key, e);
        }
        return null;
    }

    /**
     * 获取缓存值（字符串）
     *
     * @param key 缓存 key
     * @return 缓存值，不存在或已过期返回 null
     */
    public String get(String key) {
        return get(key, String.class);
    }

    /**
     * 设置缓存值 (Upsert)
     *
     * @param key   缓存 key
     * @param value 缓存值
     * @param ttl   过期时间
     */
    public <T> void set(String key, T value, Duration ttl) {
        String sql = """
                INSERT INTO %s (key, value, expires_at)
                VALUES (?, ?::jsonb, NOW() + ?::interval)
                ON CONFLICT (key) DO UPDATE
                SET value = EXCLUDED.value, expires_at = EXCLUDED.expires_at, updated_at = NOW()
                """.formatted(tableName);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, JsonUtil.toJson(value));
            ps.setString(3, ttl.toSeconds() + " seconds");
            ps.executeUpdate();
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "设置缓存失败: {}", key);
            throw new RuntimeException("Failed to set cache: " + key, e);
        }
    }

    /**
     * 设置缓存值（使用默认过期时间）
     *
     * @param key   缓存 key
     * @param value 缓存值
     */
    public <T> void set(String key, T value) {
        set(key, value, properties.getDefaultCacheExpireTime());
    }

    /**
     * 设置缓存值
     *
     * @param key        缓存 key
     * @param value      缓存值
     * @param ttlSeconds 过期时间（秒）
     */
    public <T> void set(String key, T value, long ttlSeconds) {
        set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 删除缓存
     *
     * @param key 缓存 key
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        String sql = "DELETE FROM " + tableName + " WHERE key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "删除缓存失败: {}", key);
            throw new RuntimeException("Failed to delete cache: " + key, e);
        }
    }

    /**
     * 按模式删除缓存
     *
     * @param pattern 匹配模式（支持 * 通配符）
     * @return 删除的记录数
     */
    public int deleteByPattern(String pattern) {
        String sql = "DELETE FROM " + tableName + " WHERE key LIKE ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            // 将 * 通配符转换为 SQL 的 %
            ps.setString(1, pattern.replace("*", "%"));
            return ps.executeUpdate();
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "按模式删除缓存失败: {}", pattern);
            throw new RuntimeException("Failed to delete cache by pattern: " + pattern, e);
        }
    }

    /**
     * 检查缓存是否存在且未过期
     *
     * @param key 缓存 key
     * @return 是否存在
     */
    public boolean exists(String key) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE key = ? AND expires_at > NOW()";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "检查缓存存在失败: {}", key);
            throw new RuntimeException("Failed to check cache existence: " + key, e);
        }
    }

    /**
     * 获取缓存的剩余 TTL（秒）
     *
     * @param key 缓存 key
     * @return 剩余秒数，不存在返回 -2，永不过期返回 -1
     */
    public long ttl(String key) {
        String sql = "SELECT EXTRACT(EPOCH FROM (expires_at - NOW()))::bigint AS ttl FROM " + tableName + " WHERE key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long ttl = rs.getLong("ttl");
                    return Math.max(ttl, 0);
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取缓存 TTL 失败: {}", key);
            throw new RuntimeException("Failed to get cache TTL: " + key, e);
        }
        return -2; // key 不存在
    }

    /**
     * 更新缓存的过期时间
     *
     * @param key 缓存 key
     * @param ttl 新的过期时间
     * @return 是否更新成功
     */
    public boolean expire(String key, Duration ttl) {
        String sql = "UPDATE " + tableName + " SET expires_at = NOW() + ?::interval WHERE key = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ttl.toSeconds() + " seconds");
            ps.setString(2, key);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "更新缓存过期时间失败: {}", key);
            throw new RuntimeException("Failed to update cache expiration: " + key, e);
        }
    }

    /**
     * 定时清理过期缓存
     */
    @Scheduled(fixedDelayString = "#{@postgresProperties.cacheCleanupInterval.toMillis()}")
    public void cleanup() {
        String sql = "DELETE FROM " + tableName + " WHERE expires_at < NOW()";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "清理过期缓存: {} 条", deleted);
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "清理过期缓存失败");
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存记录总数
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE expires_at > NOW()";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取缓存统计失败");
            throw new RuntimeException("Failed to get cache count", e);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        String sql = "TRUNCATE TABLE " + tableName;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
            Logs.info(EmojiSymbol.POSTGRES, "已清空所有缓存");
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "清空缓存失败");
            throw new RuntimeException("Failed to clear cache", e);
        }
    }
}
