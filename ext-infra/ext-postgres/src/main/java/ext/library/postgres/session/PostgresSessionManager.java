package ext.library.postgres.session;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.IdUtil;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PostgreSQL 会话管理器
 * <p>
 * 使用 JSONB 存储会话数据，支持：
 * <ul>
 *     <li>会话创建、读取、更新、删除</li>
 *     <li>会话数据的部分更新</li>
 *     <li>按用户 ID 查询会话</li>
 *     <li>会话续期和活动超时</li>
 *     <li>自动清理过期会话</li>
 * </ul>
 *
 * @since 4.0.0
 */
public class PostgresSessionManager {

    private final JdbcClient jdbcClient;
    private final PostgresProperties properties;
    private final String tableName;

    /**
     * Session 行映射器
     */
    private final RowMapper<Session> sessionRowMapper = (rs, _) -> new Session(
            rs.getString("session_id"),
            rs.getString("user_id"),
            rs.getString("data"),
            getInstant(rs.getTimestamp("expires_at")),
            getInstant(rs.getTimestamp("last_accessed_at")),
            getInstant(rs.getTimestamp("created_at")),
            getInstant(rs.getTimestamp("updated_at"))
    );

    public PostgresSessionManager(JdbcClient jdbcClient, PostgresProperties properties) {
        this.jdbcClient = jdbcClient;
        this.properties = properties;
        this.tableName = properties.getSessionTableName();
    }

    /**
     * 创建新会话
     *
     * @return 新会话
     */
    public Session createSession() {
        return createSession(null, Map.of());
    }

    /**
     * 创建新会话
     *
     * @param userId 用户 ID
     * @return 新会话
     */
    public Session createSession(String userId) {
        return createSession(userId, Map.of());
    }

    /**
     * 创建新会话
     *
     * @param userId      用户 ID
     * @param initialData 初始数据
     * @return 新会话
     */
    public Session createSession(String userId, Object initialData) {
        return createSession(userId, initialData, properties.getDefaultSessionTimeout());
    }

    /**
     * 创建新会话
     *
     * @param userId      用户 ID
     * @param initialData 初始数据
     * @param timeout     过期时间
     * @return 新会话
     */
    public Session createSession(String userId, Object initialData, Duration timeout) {
        String sessionId = IdUtil.getUUIDv7();

        Session session = jdbcClient.sql("""
                INSERT INTO %s (session_id, user_id, data, expires_at)
                VALUES (?, ?, ?::jsonb, ?)
                RETURNING *
                """.formatted(tableName))
                .param(sessionId)
                .param(userId)
                .param(JsonUtil.toJson(initialData))
                .param(Timestamp.from(Instant.now().plus(timeout)))
                .query(sessionRowMapper)
                .optional()
                .orElse(null);

        if (session != null) {
            Logs.debug(EmojiSymbol.POSTGRES, "创建会话: sessionId={}, userId={}", sessionId, userId);
        }
        return session;
    }

    /**
     * 获取会话
     *
     * @param sessionId 会话 ID
     * @return 会话，不存在或已过期返回空
     */
    public Optional<Session> getSession(String sessionId) {
        return getSession(sessionId, true);
    }

    /**
     * 获取会话
     *
     * @param sessionId    会话 ID
     * @param updateAccess 是否更新最后访问时间
     * @return 会话，不存在或已过期返回空
     */
    public Optional<Session> getSession(String sessionId, boolean updateAccess) {
        Optional<Session> session = jdbcClient.sql("""
                SELECT * FROM %s
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(sessionId)
                .query(sessionRowMapper)
                .optional();

        if (session.isPresent() && updateAccess) {
            touchSession(sessionId);
        }
        return session;
    }

    /**
     * 更新会话数据
     *
     * @param sessionId 会话 ID
     * @param data      新数据（完全替换）
     * @return 是否更新成功
     */
    public boolean setSessionData(String sessionId, Object data) {
        int rows = jdbcClient.sql("""
                UPDATE %s SET data = ?::jsonb, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(JsonUtil.toJson(data))
                .param(sessionId)
                .update();

        if (rows > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "更新会话数据: sessionId={}", sessionId);
        }
        return rows > 0;
    }

    /**
     * 合并更新会话数据（部分更新）
     *
     * @param sessionId 会话 ID
     * @param data      要合并的数据
     * @return 是否更新成功
     */
    public boolean mergeSessionData(String sessionId, Object data) {
        int rows = jdbcClient.sql("""
                UPDATE %s SET data = data || ?::jsonb, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(JsonUtil.toJson(data))
                .param(sessionId)
                .update();

        if (rows > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "合并会话数据: sessionId={}", sessionId);
        }
        return rows > 0;
    }

    /**
     * 设置会话属性
     *
     * @param sessionId 会话 ID
     * @param key       属性键
     * @param value     属性值
     * @return 是否设置成功
     */
    public boolean setAttribute(String sessionId, String key, Object value) {
        int rows = jdbcClient.sql("""
                UPDATE %s SET data = jsonb_set(data, ?::text[], ?::jsonb), last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param("{" + key + "}")
                .param(JsonUtil.toJson(value))
                .param(sessionId)
                .update();
        return rows > 0;
    }

    /**
     * 获取会话属性
     *
     * @param sessionId 会话 ID
     * @param key       属性键
     * @param clazz     属性类型
     * @return 属性值，不存在返回 null
     */
    public <T> T getAttribute(String sessionId, String key, Class<T> clazz) {
        return jdbcClient.sql("""
                SELECT data->? AS value FROM %s
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(key)
                .param(sessionId)
                .query(String.class)
                .optional()
                .map(value -> {
                    touchSession(sessionId);
                    return JsonUtil.readObj(value, clazz);
                })
                .orElse(null);
    }

    /**
     * 删除会话属性
     *
     * @param sessionId 会话 ID
     * @param key       属性键
     * @return 是否删除成功
     */
    public boolean removeAttribute(String sessionId, String key) {
        int rows = jdbcClient.sql("""
                UPDATE %s SET data = data - ?, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(key)
                .param(sessionId)
                .update();
        return rows > 0;
    }

    /**
     * 更新会话的最后访问时间
     *
     * @param sessionId 会话 ID
     * @return 是否更新成功
     */
    public boolean touchSession(String sessionId) {
        int rows = jdbcClient.sql("UPDATE " + tableName + " SET last_accessed_at = NOW() WHERE session_id = ?")
                .param(sessionId)
                .update();
        return rows > 0;
    }

    /**
     * 续期会话
     *
     * @param sessionId 会话 ID
     * @return 是否续期成功
     */
    public boolean renewSession(String sessionId) {
        return renewSession(sessionId, properties.getDefaultSessionTimeout());
    }

    /**
     * 续期会话
     *
     * @param sessionId 会话 ID
     * @param timeout   新的超时时间
     * @return 是否续期成功
     */
    public boolean renewSession(String sessionId, Duration timeout) {
        int rows = jdbcClient.sql("""
                UPDATE %s SET expires_at = NOW() + ?::interval, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(timeout.toSeconds() + " seconds")
                .param(sessionId)
                .update();

        if (rows > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "续期会话: sessionId={}, timeout={}", sessionId, timeout);
        }
        return rows > 0;
    }

    /**
     * 绑定用户 ID 到会话
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @return 是否绑定成功
     */
    public boolean bindUser(String sessionId, String userId) {
        int rows = jdbcClient.sql("""
                UPDATE %s SET user_id = ?, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName))
                .param(userId)
                .param(sessionId)
                .update();

        if (rows > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "绑定用户: sessionId={}, userId={}", sessionId, userId);
        }
        return rows > 0;
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话 ID
     * @return 是否删除成功
     */
    public boolean deleteSession(String sessionId) {
        int rows = jdbcClient.sql("DELETE FROM " + tableName + " WHERE session_id = ?")
                .param(sessionId)
                .update();

        if (rows > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "删除会话: sessionId={}", sessionId);
        }
        return rows > 0;
    }

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户 ID
     * @return 会话列表
     */
    public List<Session> getSessionsByUser(String userId) {
        return jdbcClient.sql("""
                SELECT * FROM %s
                WHERE user_id = ? AND expires_at > NOW()
                ORDER BY last_accessed_at DESC
                """.formatted(tableName))
                .param(userId)
                .query(sessionRowMapper)
                .list();
    }

    /**
     * 删除用户的所有会话
     *
     * @param userId 用户 ID
     * @return 删除的会话数
     */
    public int deleteSessionsByUser(String userId) {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE user_id = ?")
                .param(userId)
                .update();

        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "删除用户所有会话: userId={}, count={}", userId, deleted);
        }
        return deleted;
    }

    /**
     * 检查会话是否存在且有效
     *
     * @param sessionId 会话 ID
     * @return 是否存在
     */
    public boolean exists(String sessionId) {
        return jdbcClient.sql("SELECT 1 FROM " + tableName + " WHERE session_id = ? AND expires_at > NOW()")
                .param(sessionId)
                .query(Integer.class)
                .optional()
                .isPresent();
    }

    /**
     * 获取当前有效会话数量
     *
     * @return 有效会话数量
     */
    public long count() {
        return jdbcClient.sql("SELECT COUNT(*) FROM " + tableName + " WHERE expires_at > NOW()")
                .query(Long.class)
                .single();
    }

    /**
     * 定时清理过期会话和不活跃会话
     */
    @Scheduled(fixedDelayString = "#{@postgresProperties.sessionCleanupInterval.toMillis()}")
    public void cleanup() {
        cleanupExpired();
        cleanupInactive();
    }

    /**
     * 清理过期会话
     */
    private void cleanupExpired() {
        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE expires_at < NOW()")
                .update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理过期会话: {} 条", deleted);
        }
    }

    /**
     * 清理不活跃会话
     */
    private void cleanupInactive() {
        Duration activityTimeout = properties.getSessionActivityTimeout();
        if (activityTimeout.isZero() || activityTimeout.isNegative()) {
            return;
        }

        int deleted = jdbcClient.sql("DELETE FROM " + tableName + " WHERE last_accessed_at < NOW() - ?::interval")
                .param(activityTimeout.toSeconds() + " seconds")
                .update();
        if (deleted > 0) {
            Logs.debug(EmojiSymbol.POSTGRES, "清理不活跃会话: {} 条", deleted);
        }
    }

    /**
     * 从 Timestamp 获取 Instant 类型的时间戳
     *
     * @param ts Timestamp 时间戳
     * @return Instant 时间戳，如果为 null 则返回 null
     */
    private Instant getInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }
}
