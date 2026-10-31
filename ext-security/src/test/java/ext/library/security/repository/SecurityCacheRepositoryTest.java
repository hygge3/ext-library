package ext.library.security.repository;

import ext.library.cache.strategy.CacheStrategy;
import ext.library.security.domain.SecuritySession;
import ext.library.security.domain.SecurityToken;
import ext.library.security.enums.TokenState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 缓存后端（Caffeine、L2 本地层）的 {@code get} 返回的是缓存里的同一个对象引用，
 * 调用方就地改写会污染缓存对象 —— 仓库必须给出副本。
 */
@DisplayName("安全缓存仓库")
class SecurityCacheRepositoryTest {

    private final SecurityCacheRepository repository = new SecurityCacheRepository(new SameInstanceCache());

    @Test
    @DisplayName("读出的是会话副本，就地改写不影响缓存")
    void returnsDetachedSessionCopy() {
        SecuritySession session = new SecuritySession();
        session.setLoginId("u1");
        session.setSecuritySessionId("s1");
        session.setTimeout(3600L);
        session.setCreateTime(LocalDateTime.now());
        session.addTokenInfo(token("t1"));
        repository.saveSecuritySession(session);

        SecuritySession first = repository.getSecuritySessionByLoginId("u1");
        first.setTokenInfoList(null);
        first.setCurrentSecurityToken(null);

        SecuritySession second = repository.getSecuritySessionByLoginId("u1");
        assertThat(second).isNotNull();
        assertThat(second.getTokenInfoList()).hasSize(1);
        assertThat(second.getTokenInfoList().getFirst().getToken()).isEqualTo("t1");
    }

    @Test
    @DisplayName("读出的是 token 副本，就地改写不影响缓存")
    void returnsDetachedTokenCopy() {
        repository.saveToken(token("t1"));

        repository.getSecurityTokenByTokenValue("t1").setState(TokenState.BANNED);

        assertThat(repository.getSecurityTokenByTokenValue("t1").getState()).isNotEqualTo(TokenState.BANNED);
    }

    private SecurityToken token(String value) {
        SecurityToken token = new SecurityToken();
        token.setToken(value);
        token.setLoginId("u1");
        token.setDeviceType("PC");
        token.setTimeout(3600L);
        return token;
    }

    /**
     * 返回同一实例的内存存储，等价于 Caffeine / L2 本地层的行为
     */
    private static class SameInstanceCache implements CacheStrategy {

        private final Map<String, Object> store = new ConcurrentHashMap<>();

        @Override
        public <T> T get(String cacheName, String key, Type type) {
            return narrow(store.get(cacheName + ":" + key), type);
        }

        @Override
        public <T> T put(String cacheName, String key, T value, Duration expireTime) {
            store.put(cacheName + ":" + key, value);
            return value;
        }

        @Override
        public <T> T put(String cacheName, String key, T value) {
            return put(cacheName, key, value, Duration.ofDays(1));
        }

        @Override
        public void evict(String cacheName, String key) {
            store.remove(cacheName + ":" + key);
        }

        @Override
        public void clear(String cacheName) {
            store.keySet().removeIf(key -> key.startsWith(cacheName + ":"));
        }

        @Override
        public boolean putIfAbsent(String cacheName, String key, Object value, Duration expireTime) {
            return store.putIfAbsent(cacheName + ":" + key, value) == null;
        }

    }

}
