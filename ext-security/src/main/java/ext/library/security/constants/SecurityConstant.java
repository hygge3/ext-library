package ext.library.security.constants;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 安全模块常量
 */
public final class SecurityConstant {

    private SecurityConstant() {
        // 禁止实例化
    }

    /**
     * 计算剩余超时秒数
     *
     * @param baseTime       基准时间（创建时间或活跃时间）
     * @param timeoutSeconds 超时时间（秒）
     * @return 剩余秒数，若已过期返回 0
     */
    public static long calculateRemainingSeconds(LocalDateTime baseTime, long timeoutSeconds) {
        if (NON_EXPIRING.equals(timeoutSeconds)) {
            return NON_EXPIRING;
        }
        long remaining = Duration.between(LocalDateTime.now(), baseTime.plusSeconds(timeoutSeconds)).toSeconds();
        return Math.max(0L, remaining);
    }

    /**
     * 判断是否已超时
     *
     * @param baseTime       基准时间
     * @param timeoutSeconds 超时时间（秒）
     * @return true 已超时，false 未超时
     */
    public static boolean isExpired(LocalDateTime baseTime, long timeoutSeconds) {
        if (NON_EXPIRING.equals(timeoutSeconds)) {
            return false;
        }
        return baseTime.plusSeconds(timeoutSeconds).isBefore(LocalDateTime.now());
    }

    /**
     * 不过期（-1L）
     */
    public static final Long NON_EXPIRING = -1L;

    /**
     * 不限制（-1）
     */
    public static final Integer NON_LIMIT = -1;

    /**
     * Authorization 前缀
     */
    public static final String AUTHORIZATION_PREFIX = "Bearer ";

    /**
     * 未知
     */
    public static final String UNKNOWN = "unknown";

    /**
     * Security Session ID
     */
    public static final String SECURITY_SESSION_ID = "security_session_id";

    /**
     * 自定义 Token 参数名称
     */
    public static final String SECURITY_CUSTOM_IDENTITY_TOKEN = "security_custom_identity_token";

    /**
     * 默认超时时间：30 天（秒）
     */
    public static final long DEFAULT_TIMEOUT_SECONDS = 60L * 60 * 24 * 30;

    /**
     * 默认活跃超时时间：1 小时（秒）
     */
    public static final long DEFAULT_ACTIVITY_TIMEOUT_SECONDS = 60L * 60;

    /**
     * 无效 Token 清理阈值：48 小时
     */
    public static final int INVALID_TOKEN_CLEANUP_HOURS = 48;
}
