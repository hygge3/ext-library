package ext.library.postgres.cache;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

/**
 * PostgreSQL 缓存管理器
 * <p>
 * 使用 UNLOGGED 表实现高性能缓存，支持 TTL 过期和自动清理
 *
 * @since 4.0.0
 */
public class PostgresCacheManager {

    private final JdbcClient jdbcClient;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresCacheManager(JdbcClient jdbcClient, PostgresProperties properties) {
        this.jdbcClient = jdbcClient;
        this.properties = properties;
        this.tableName = properties.getCacheTableName();
    }

    /**
     * 获取缓存值
     *
     * @param key   缓存 key
     * @param clazz 值类型
     *
     * @return 缓存值，不存在或已过期返回 null
     */
    public <T> T get(String key, Class<T> clazz) {
        return jdbcClient.sql("SELECT value FROM " + tableName + " WHERE key = ? AND expires_at > NOW()")
                .param(key)
                .query(String.class)
                .optional()
                .map(json -> JsonUtil.readObj(json, clazz))
                .orElse(null);
    }

    /**
     * 获取缓存值（字符串）
     *
     * @param key 缓存 key
     *
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
        jdbcClient.sql("""
                INSERT INTO %s (key, value, expires_at)
                VALUES (?, ?::jsonb, NOW() + ?::interval)
                ON CONFLICT (key) DO UPDATE
                SET value = EXCLUDED.value, expires_at = EXCLUDED.expires_at, updated_at = NOW()
                """.formatted(tableName)).param(key).param(JsonUtil.toJson(value)).param(ttl.toSeconds() + " seconds").update();
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
     *
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        int rows = jdbcClient.sql("DELETE FROM " + tableName + " WHERE key = ?").param(key).update();
        return rows > 0;
    }

    /**
     * 按模式删除缓存
     *
     * @param pattern 匹配模式（支持 * 通配符）
     *
     * @return 删除的记录数
     */
    public int deleteByPattern(String pattern) {
        // 将 * 通配符转换为 SQL 的 %
        return jdbcClient.sql("DELETE FROM " + tableName + " WHERE key LIKE ?").param(pattern.replace("*", "%")).update();
    }

    /**
     * 检查缓存是否存在且未过期
     *
     * @param key 缓存 key
     *
     * @return 是否存在
     */
    public boolean exists(String key) {
        return jdbcClient.sql("SELECT 1 FROM " + tableName + " WHERE key = ? AND expires_at > NOW()")
                .param(key)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    /**
     * 获取缓存的剩余 TTL（秒）
     *
     * @param key 缓存 key
     *
     * @return 剩余秒数，不存在返回 -2，永不过期返回 -1
     */
    public long ttl(String key) {
        return jdbcClient.sql("SELECT EXTRACT(EPOCH FROM (expires_at - NOW()))::bigint AS ttl FROM " + tableName + " WHERE key = ?")
                .param(key)
                .query(Long.class)
                .optional()
                .map(ttl -> Math.max(ttl, 0))
                .orElse(-2L);
    }

    /**
     * 更新缓存的过期时间
     *
     * @param key 缓存 key
     * @param ttl 新的过期时间
     *
     * @return 是否更新成功
     */
    public boolean expire(String key, Duration ttl) {
        int rows = jdbcClient.sql("UPDATE " + tableName + " SET expires_at = NOW() + ?::interval WHERE key = ?")
                .param(ttl.toSeconds() + " seconds")
                .param(key)
                .update();
        return rows > 0;
    }

    /**
     * 定时清理过期缓存
     */
    @Scheduled(fixedDelayString = "#{@postgresProperties.cacheCleanupInterval.toMillis()}")
    public void cleanup() {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE expires_at < NOW()").update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理过期缓存: {} 条", deleted);
        }
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存记录总数
     */
    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE expires_at > NOW()").query(Long.class).single();
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        jdbcClient.sql("TRUNCATE TABLE %s".formatted(tableName)).update();
        Logs.info(EmojiSymbol.POSTGRES, "已清空所有缓存");
    }
}
