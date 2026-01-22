package ext.library.postgres.session;

import ext.library.json.util.JsonUtil;
import ext.library.postgres.properties.PostgresProperties;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.util.IdUtil;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    private final DataSource dataSource;
    private final PostgresProperties properties;
    private final String tableName;

    public PostgresSessionManager(DataSource dataSource, PostgresProperties properties) {
        this.dataSource = dataSource;
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
        String sql = """
                INSERT INTO %s (session_id, user_id, data, expires_at)
                VALUES (?, ?, ?::jsonb, ?)
                RETURNING *
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setString(2, userId);
            ps.setString(3, JsonUtil.toJson(initialData));
            ps.setTimestamp(4, Timestamp.from(Instant.now().plus(timeout)));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session session = mapToSession(rs);
                    Logs.debug(EmojiSymbol.POSTGRES, "创建会话: sessionId={}, userId={}", sessionId, userId);
                    return session;
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "创建会话失败: userId={}", userId);
            throw new RuntimeException("Failed to create session", e);
        }
        return null;
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
        String sql = """
                SELECT * FROM %s
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Session session = mapToSession(rs);
                    if (updateAccess) {
                        touchSession(sessionId);
                    }
                    return Optional.of(session);
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取会话失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to get session: " + sessionId, e);
        }
        return Optional.empty();
    }

    /**
     * 更新会话数据
     *
     * @param sessionId 会话 ID
     * @param data      新数据（完全替换）
     * @return 是否更新成功
     */
    public boolean setSessionData(String sessionId, Object data) {
        String sql = """
                UPDATE %s SET data = ?::jsonb, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, JsonUtil.toJson(data));
            ps.setString(2, sessionId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                Logs.debug(EmojiSymbol.POSTGRES, "更新会话数据: sessionId={}", sessionId);
            }
            return updated;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "更新会话数据失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to update session data: " + sessionId, e);
        }
    }

    /**
     * 合并更新会话数据（部分更新）
     *
     * @param sessionId 会话 ID
     * @param data      要合并的数据
     * @return 是否更新成功
     */
    public boolean mergeSessionData(String sessionId, Object data) {
        String sql = """
                UPDATE %s SET data = data || ?::jsonb, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, JsonUtil.toJson(data));
            ps.setString(2, sessionId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                Logs.debug(EmojiSymbol.POSTGRES, "合并会话数据: sessionId={}", sessionId);
            }
            return updated;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "合并会话数据失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to merge session data: " + sessionId, e);
        }
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
        String sql = """
                UPDATE %s SET data = jsonb_set(data, ?::text[], ?::jsonb), last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "{" + key + "}");
            ps.setString(2, JsonUtil.toJson(value));
            ps.setString(3, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "设置会话属性失败: sessionId={}, key={}", sessionId, key);
            throw new RuntimeException("Failed to set session attribute: " + sessionId, e);
        }
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
        String sql = """
                SELECT data->? AS value FROM %s
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String value = rs.getString("value");
                    if (value != null) {
                        touchSession(sessionId);
                        return JsonUtil.readObj(value, clazz);
                    }
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取会话属性失败: sessionId={}, key={}", sessionId, key);
            throw new RuntimeException("Failed to get session attribute: " + sessionId, e);
        }
        return null;
    }

    /**
     * 删除会话属性
     *
     * @param sessionId 会话 ID
     * @param key       属性键
     * @return 是否删除成功
     */
    public boolean removeAttribute(String sessionId, String key) {
        String sql = """
                UPDATE %s SET data = data - ?, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "删除会话属性失败: sessionId={}, key={}", sessionId, key);
            throw new RuntimeException("Failed to remove session attribute: " + sessionId, e);
        }
    }

    /**
     * 更新会话的最后访问时间
     *
     * @param sessionId 会话 ID
     * @return 是否更新成功
     */
    public boolean touchSession(String sessionId) {
        String sql = "UPDATE " + tableName + " SET last_accessed_at = NOW() WHERE session_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "更新会话访问时间失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to touch session: " + sessionId, e);
        }
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
        String sql = """
                UPDATE %s SET expires_at = NOW() + ?::interval, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, timeout.toSeconds() + " seconds");
            ps.setString(2, sessionId);
            boolean renewed = ps.executeUpdate() > 0;
            if (renewed) {
                Logs.debug(EmojiSymbol.POSTGRES, "续期会话: sessionId={}, timeout={}", sessionId, timeout);
            }
            return renewed;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "续期会话失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to renew session: " + sessionId, e);
        }
    }

    /**
     * 绑定用户 ID 到会话
     *
     * @param sessionId 会话 ID
     * @param userId    用户 ID
     * @return 是否绑定成功
     */
    public boolean bindUser(String sessionId, String userId) {
        String sql = """
                UPDATE %s SET user_id = ?, last_accessed_at = NOW()
                WHERE session_id = ? AND expires_at > NOW()
                """.formatted(tableName);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, sessionId);
            boolean bound = ps.executeUpdate() > 0;
            if (bound) {
                Logs.debug(EmojiSymbol.POSTGRES, "绑定用户: sessionId={}, userId={}", sessionId, userId);
            }
            return bound;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "绑定用户失败: sessionId={}, userId={}", sessionId, userId);
            throw new RuntimeException("Failed to bind user to session: " + sessionId, e);
        }
    }

    /**
     * 删除会话
     *
     * @param sessionId 会话 ID
     * @return 是否删除成功
     */
    public boolean deleteSession(String sessionId) {
        String sql = "DELETE FROM " + tableName + " WHERE session_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                Logs.debug(EmojiSymbol.POSTGRES, "删除会话: sessionId={}", sessionId);
            }
            return deleted;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "删除会话失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to delete session: " + sessionId, e);
        }
    }

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户 ID
     * @return 会话列表
     */
    public List<Session> getSessionsByUser(String userId) {
        String sql = """
                SELECT * FROM %s
                WHERE user_id = ? AND expires_at > NOW()
                ORDER BY last_accessed_at DESC
                """.formatted(tableName);

        List<Session> sessions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapToSession(rs));
                }
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取用户会话列表失败: userId={}", userId);
            throw new RuntimeException("Failed to get sessions by user: " + userId, e);
        }
        return sessions;
    }

    /**
     * 删除用户的所有会话
     *
     * @param userId 用户 ID
     * @return 删除的会话数
     */
    public int deleteSessionsByUser(String userId) {
        String sql = "DELETE FROM " + tableName + " WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "删除用户所有会话: userId={}, count={}", userId, deleted);
            }
            return deleted;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "删除用户会话失败: userId={}", userId);
            throw new RuntimeException("Failed to delete sessions by user: " + userId, e);
        }
    }

    /**
     * 检查会话是否存在且有效
     *
     * @param sessionId 会话 ID
     * @return 是否存在
     */
    public boolean exists(String sessionId) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE session_id = ? AND expires_at > NOW()";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "检查会话是否存在失败: sessionId={}", sessionId);
            throw new RuntimeException("Failed to check session existence: " + sessionId, e);
        }
    }

    /**
     * 获取当前有效会话数量
     *
     * @return 有效会话数量
     */
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE expires_at > NOW()";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "获取会话数量失败");
            throw new RuntimeException("Failed to count sessions", e);
        }
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
        String sql = "DELETE FROM " + tableName + " WHERE expires_at < NOW()";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "清理过期会话: {} 条", deleted);
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "清理过期会话失败");
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

        String sql = "DELETE FROM " + tableName + " WHERE last_accessed_at < NOW() - ?::interval";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, activityTimeout.toSeconds() + " seconds");
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                Logs.debug(EmojiSymbol.POSTGRES, "清理不活跃会话: {} 条", deleted);
            }
        } catch (SQLException e) {
            Logs.error(EmojiSymbol.POSTGRES, e, "清理不活跃会话失败");
        }
    }

    /**
     * 将查询结果映射为会话对象
     *
     * @param rs 查询结果集
     * @return 会话对象
     * @throws SQLException 如果读取结果集失败
     */
    private Session mapToSession(ResultSet rs) throws SQLException {
        return new Session(
                rs.getString("session_id"),
                rs.getString("user_id"),
                rs.getString("data"),
                getInstant(rs, "expires_at"),
                getInstant(rs, "last_accessed_at"),
                getInstant(rs, "created_at"),
                getInstant(rs, "updated_at")
        );
    }

    /**
     * 从 ResultSet 获取 Instant 类型的时间戳
     *
     * @param rs     查询结果集
     * @param column 列名
     * @return Instant 时间戳，如果为 null 则返回 null
     * @throws SQLException 如果读取结果集失败
     */
    private Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}
