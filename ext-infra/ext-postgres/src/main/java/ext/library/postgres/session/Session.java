package ext.library.postgres.session;

import ext.library.json.util.JsonUtil;

import java.time.Instant;
import java.util.Map;

/**
 * 会话记录
 *
 * @param sessionId      会话 ID
 * @param userId         用户 ID（可选）
 * @param data           会话数据（JSON 字符串）
 * @param expiresAt      过期时间
 * @param lastAccessedAt 最后访问时间
 * @param createdAt      创建时间
 * @param updatedAt      更新时间
 * @since 4.0.0
 */
public record Session(
        String sessionId,
        String userId,
        String data,
        Instant expiresAt,
        Instant lastAccessedAt,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * 获取会话数据并反序列化为指定类型
     *
     * @param clazz 目标类型
     * @return 反序列化后的对象
     */
    public <T> T getData(Class<T> clazz) {
        return JsonUtil.readObj(data, clazz);
    }

    /**
     * 获取会话数据为 Map
     *
     * @return 会话数据 Map
     */
    public Map<String, Object> getDataAsMap() {
        return JsonUtil.readMap(data);
    }

    /**
     * 检查会话是否已过期
     *
     * @return 是否已过期
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * 获取剩余有效时间（毫秒）
     *
     * @return 剩余毫秒数，已过期返回 0
     */
    public long getRemainingTimeMillis() {
        long remaining = expiresAt.toEpochMilli() - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * 获取剩余有效时间（秒）
     *
     * @return 剩余秒数，已过期返回 0
     */
    public long getRemainingTimeSeconds() {
        return getRemainingTimeMillis() / 1000;
    }
}
