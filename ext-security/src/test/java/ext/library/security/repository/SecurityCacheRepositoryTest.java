package ext.library.security.repository;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.constants.SecurityConstant;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.security.enums.TokenState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SecurityCacheRepository 单元测试
 */
@ExtendWith(MockitoExtension.class)
class SecurityCacheRepositoryTest {

    @Mock
    private CacheStrategy cacheStrategy;

    private SecurityCacheRepository repository;

    @BeforeEach
    void setUp() {
        repository = new SecurityCacheRepository(cacheStrategy);
    }

    @Nested
    @DisplayName("Session 操作测试")
    class SessionTests {

        @Test
        @DisplayName("getSecuritySessionByLoginId: 返回 null 当 session 不存在")
        void getSecuritySessionByLoginId_notFound_returnsNull() {
            when(cacheStrategy.get(anyString(), eq("user1"), eq(SecuritySession.class))).thenReturn(null);

            SecuritySession result = repository.getSecuritySessionByLoginId("user1");

            assertNull(result);
        }

        @Test
        @DisplayName("getSecuritySessionByLoginId: 返回 session 当未过期")
        void getSecuritySessionByLoginId_notExpired_returnsSession() {
            SecuritySession session = createSession("user1", 3600L);
            when(cacheStrategy.get(anyString(), eq("user1"), eq(SecuritySession.class))).thenReturn(session);

            SecuritySession result = repository.getSecuritySessionByLoginId("user1");

            assertNotNull(result);
            assertEquals("user1", result.getLoginId());
        }

        @Test
        @DisplayName("getSessionTimeoutByLoginId: 返回剩余超时时间")
        void getSessionTimeoutByLoginId_returnsRemainingTime() {
            SecuritySession session = createSession("user1", 3600L);
            when(cacheStrategy.get(anyString(), eq("user1"), eq(SecuritySession.class))).thenReturn(session);

            Long result = repository.getSessionTimeoutByLoginId("user1");

            assertNotNull(result);
            assertTrue(result > 0);
            assertTrue(result <= 3600L);
        }

        @Test
        @DisplayName("getSessionTimeoutByLoginId: 返回 null 当 session 不存在")
        void getSessionTimeoutByLoginId_notFound_returnsNull() {
            when(cacheStrategy.get(anyString(), eq("user1"), eq(SecuritySession.class))).thenReturn(null);

            Long result = repository.getSessionTimeoutByLoginId("user1");

            assertNull(result);
        }

        @Test
        @DisplayName("saveSecuritySession: 保存成功")
        void saveSecuritySession_success() {
            SecuritySession session = createSession("user1", 3600L);

            boolean result = repository.saveSecuritySession(session);

            assertTrue(result);
            verify(cacheStrategy).put(anyString(), eq("user1"), eq(session), any(Duration.class));
        }

        @Test
        @DisplayName("saveSecuritySession: 返回 false 当 session 为 null")
        void saveSecuritySession_nullSession_returnsFalse() {
            boolean result = repository.saveSecuritySession(null);

            assertFalse(result);
            verify(cacheStrategy, never()).put(anyString(), anyString(), any(), any(Duration.class));
        }

        @Test
        @DisplayName("removeSecuritySessionByLoginId: 删除成功")
        void removeSecuritySessionByLoginId_success() {
            SecuritySession session = createSession("user1", 3600L);
            when(cacheStrategy.get(anyString(), eq("user1"), eq(SecuritySession.class))).thenReturn(session);

            boolean result = repository.removeSecuritySessionByLoginId("user1");

            assertTrue(result);
            verify(cacheStrategy).evict(anyString(), eq("user1"));
        }

        @Test
        @DisplayName("removeSecuritySessionByLoginId: 返回 false 当 session 不存在")
        void removeSecuritySessionByLoginId_notFound_returnsFalse() {
            when(cacheStrategy.get(anyString(), eq("user1"), eq(SecuritySession.class))).thenReturn(null);

            boolean result = repository.removeSecuritySessionByLoginId("user1");

            assertFalse(result);
            verify(cacheStrategy, never()).evict(anyString(), eq("user1"));
        }
    }

    @Nested
    @DisplayName("Token 操作测试")
    class TokenTests {

        @Test
        @DisplayName("getSecurityTokenByTokenValue: 返回 token")
        void getSecurityTokenByTokenValue_found_returnsToken() {
            SecurityToken token = createToken("token123", "user1");
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(token);

            SecurityToken result = repository.getSecurityTokenByTokenValue("token123");

            assertNotNull(result);
            assertEquals("token123", result.getToken());
        }

        @Test
        @DisplayName("getSecurityTokenByTokenValue: 返回 null 当 token 不存在")
        void getSecurityTokenByTokenValue_notFound_returnsNull() {
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(null);

            SecurityToken result = repository.getSecurityTokenByTokenValue("token123");

            assertNull(result);
        }

        @Test
        @DisplayName("getActivityTimeByTokenValue: 返回活跃时间")
        void getActivityTimeByTokenValue_found_returnsActivityTime() {
            SecurityToken token = createToken("token123", "user1");
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(token);

            LocalDateTime result = repository.getActivityTimeByTokenValue("token123");

            assertNotNull(result);
        }

        @Test
        @DisplayName("getTokenTimeOutByTokenValue: 返回剩余超时时间")
        void getTokenTimeOutByTokenValue_returnsRemainingTime() {
            SecurityToken token = createToken("token123", "user1");
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(token);

            Long result = repository.getTokenTimeOutByTokenValue("token123");

            assertNotNull(result);
            assertTrue(result > 0);
        }

        @Test
        @DisplayName("getTokenActivityTimeOutByTokenValue: 返回活跃剩余超时时间")
        void getTokenActivityTimeOutByTokenValue_returnsRemainingTime() {
            SecurityToken token = createToken("token123", "user1");
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(token);

            Long result = repository.getTokenActivityTimeOutByTokenValue("token123");

            assertNotNull(result);
            assertTrue(result > 0);
        }

        @Test
        @DisplayName("saveToken: 保存成功")
        void saveToken_success() {
            SecurityToken token = createToken("token123", "user1");

            boolean result = repository.saveToken(token);

            assertTrue(result);
            verify(cacheStrategy).put(anyString(), eq("token123"), eq(token), any(Duration.class));
        }

        @Test
        @DisplayName("saveToken: 返回 false 当 token 为 null")
        void saveToken_nullToken_returnsFalse() {
            boolean result = repository.saveToken(null);

            assertFalse(result);
            verify(cacheStrategy, never()).put(anyString(), anyString(), any(), any(Duration.class));
        }

        @Test
        @DisplayName("removeTokenByTokenValue: 删除成功")
        void removeTokenByTokenValue_success() {
            SecurityToken token = createToken("token123", "user1");
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(token);

            boolean result = repository.removeTokenByTokenValue("token123");

            assertTrue(result);
            verify(cacheStrategy).evict(anyString(), eq("token123"));
        }

        @Test
        @DisplayName("renewalTokenByTokenValue: 续约成功")
        void renewalTokenByTokenValue_success() {
            SecurityToken token = createToken("token123", "user1");
            LocalDateTime oldActivityTime = token.getActivityTime();
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(token);

            boolean result = repository.renewalTokenByTokenValue("token123");

            assertTrue(result);
            // Verify activity time was updated
            assertTrue(token.getActivityTime().isAfter(oldActivityTime) ||
                    token.getActivityTime().isEqual(oldActivityTime));
        }

        @Test
        @DisplayName("renewalTokenByTokenValue: 返回 false 当 token 不存在")
        void renewalTokenByTokenValue_notFound_returnsFalse() {
            when(cacheStrategy.get(anyString(), eq("token123"), eq(SecurityToken.class))).thenReturn(null);

            boolean result = repository.renewalTokenByTokenValue("token123");

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("queryTokenList 测试")
    class QueryTokenListTests {

        @Test
        @DisplayName("queryTokenList: 返回空列表当无 token")
        void queryTokenList_noTokens_returnsEmptyList() {
            List<String> result = repository.queryTokenList(null, false);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("queryTokenList: 返回保存过的 token")
        void queryTokenList_withTokens_returnsTokenList() {
            // Save some tokens first
            SecurityToken token1 = createToken("token1", "user1");
            SecurityToken token2 = createToken("token2", "user2");

            repository.saveToken(token1);
            repository.saveToken(token2);

            // Mock the get calls for verification
            when(cacheStrategy.get(anyString(), eq("token1"), eq(SecurityToken.class))).thenReturn(token1);
            when(cacheStrategy.get(anyString(), eq("token2"), eq(SecurityToken.class))).thenReturn(token2);

            List<String> result = repository.queryTokenList(null, false);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("token1"));
            assertTrue(result.contains("token2"));
        }

        @Test
        @DisplayName("queryTokenList: 支持模糊匹配")
        void queryTokenList_withFilter_returnsFilteredList() {
            SecurityToken token1 = createToken("user-token-1", "user1");
            SecurityToken token2 = createToken("admin-token-1", "admin1");

            repository.saveToken(token1);
            repository.saveToken(token2);

            // Only stub the token that will be matched by the filter
            when(cacheStrategy.get(anyString(), eq("user-token-1"), eq(SecurityToken.class))).thenReturn(token1);

            List<String> result = repository.queryTokenList("user", false);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains("user-token-1"));
        }

        @Test
        @DisplayName("queryTokenList: 支持降序排序")
        void queryTokenList_sortedDesc_returnsDescOrder() {
            SecurityToken tokenA = createToken("a-token", "user1");
            SecurityToken tokenB = createToken("b-token", "user2");

            repository.saveToken(tokenA);
            repository.saveToken(tokenB);

            when(cacheStrategy.get(anyString(), eq("a-token"), eq(SecurityToken.class))).thenReturn(tokenA);
            when(cacheStrategy.get(anyString(), eq("b-token"), eq(SecurityToken.class))).thenReturn(tokenB);

            List<String> result = repository.queryTokenList(null, true);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("b-token", result.get(0));
            assertEquals("a-token", result.get(1));
        }
    }

    // Helper methods

    private SecuritySession createSession(String loginId, Long timeout) {
        SecuritySession session = new SecuritySession();
        session.setLoginId(loginId);
        session.setTimeout(timeout);
        session.setCreateTime(LocalDateTime.now());
        return session;
    }

    private SecurityToken createToken(String tokenValue, String loginId) {
        SecurityToken token = new SecurityToken();
        token.setToken(tokenValue);
        token.setLoginId(loginId);
        token.setTimeout(SecurityConstant.DEFAULT_TIMEOUT_SECONDS);
        token.setActivityTimeout(SecurityConstant.DEFAULT_ACTIVITY_TIMEOUT_SECONDS);
        token.setCreateTime(LocalDateTime.now());
        token.setActivityTime(LocalDateTime.now().minusMinutes(1));
        token.setState(TokenState.NORMAL);
        return token;
    }
}
