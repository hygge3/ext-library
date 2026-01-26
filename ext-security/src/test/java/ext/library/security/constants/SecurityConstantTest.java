package ext.library.security.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SecurityConstant 单元测试
 */
class SecurityConstantTest {

    @Test
    @DisplayName("calculateRemainingSeconds: 永不过期时返回 NON_EXPIRING")
    void calculateRemainingSeconds_nonExpiring_returnsNonExpiring() {
        LocalDateTime baseTime = LocalDateTime.now();
        long result = SecurityConstant.calculateRemainingSeconds(baseTime, SecurityConstant.NON_EXPIRING);
        assertEquals(SecurityConstant.NON_EXPIRING, result);
    }

    @Test
    @DisplayName("calculateRemainingSeconds: 未超时时返回正数")
    void calculateRemainingSeconds_notExpired_returnsPositive() {
        LocalDateTime baseTime = LocalDateTime.now();
        long timeoutSeconds = 3600L; // 1 小时
        long result = SecurityConstant.calculateRemainingSeconds(baseTime, timeoutSeconds);
        assertTrue(result > 0, "剩余时间应为正数");
        assertTrue(result <= timeoutSeconds, "剩余时间不应超过超时时间");
    }

    @Test
    @DisplayName("calculateRemainingSeconds: 已超时时返回 0")
    void calculateRemainingSeconds_expired_returnsZero() {
        LocalDateTime baseTime = LocalDateTime.now().minusHours(2);
        long timeoutSeconds = 3600L; // 1 小时
        long result = SecurityConstant.calculateRemainingSeconds(baseTime, timeoutSeconds);
        assertEquals(0L, result);
    }

    @Test
    @DisplayName("isExpired: 永不过期时返回 false")
    void isExpired_nonExpiring_returnsFalse() {
        LocalDateTime baseTime = LocalDateTime.now().minusYears(10);
        boolean result = SecurityConstant.isExpired(baseTime, SecurityConstant.NON_EXPIRING);
        assertFalse(result);
    }

    @Test
    @DisplayName("isExpired: 未超时时返回 false")
    void isExpired_notExpired_returnsFalse() {
        LocalDateTime baseTime = LocalDateTime.now();
        long timeoutSeconds = 3600L; // 1 小时
        boolean result = SecurityConstant.isExpired(baseTime, timeoutSeconds);
        assertFalse(result);
    }

    @Test
    @DisplayName("isExpired: 已超时时返回 true")
    void isExpired_expired_returnsTrue() {
        LocalDateTime baseTime = LocalDateTime.now().minusHours(2);
        long timeoutSeconds = 3600L; // 1 小时
        boolean result = SecurityConstant.isExpired(baseTime, timeoutSeconds);
        assertTrue(result);
    }
}
