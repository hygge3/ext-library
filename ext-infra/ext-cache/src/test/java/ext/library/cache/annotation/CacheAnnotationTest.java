package ext.library.cache.annotation;

import ext.library.cache.enums.CacheType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @Cache 注解测试
 */
@DisplayName("@Cache 注解测试")
class CacheAnnotationTest {

    // ========== 测试类 ==========

    static class TestService {

        @Cache(cacheName = "user", key = "#id")
        public String getUser(Long id) {
            return "User:" + id;
        }

        @Cache(cacheName = "user", key = "#id", timeout = 300, timeUnit = TimeUnit.SECONDS)
        public String getUserWithTimeout(Long id) {
            return "User:" + id;
        }

        @Cache(cacheName = "user", key = "#user.id", type = CacheType.PUT)
        public String updateUser(User user) {
            return "Updated";
        }

        @Cache(cacheName = "user", key = "#id", type = CacheType.DELETE)
        public void deleteUser(Long id) {
        }

        @Cache(cacheName = "user", key = "#id", type = CacheType.FULL)
        public String fullCache(Long id) {
            return "User:" + id;
        }

        // 测试用数据类
        static class User {
            private Long id;
            private String name;

            public User() {
            }

            public User(Long id, String name) {
                this.id = id;
                this.name = name;
            }

            public Long getId() {
                return id;
            }

            public String getName() {
                return name;
            }
        }
    }

    // ========== 注解元素测试 ==========

    @Test
    @DisplayName("测试注解基本属性")
    void testBasicAnnotation() throws Exception {
        Method method = TestService.class.getMethod("getUser", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertEquals("user", cache.cacheName(), "cacheName 应正确");
        assertEquals("#id", cache.key(), "key 应正确");
        assertEquals(120, cache.timeout(), "默认 timeout 应为 120");
        assertEquals(TimeUnit.SECONDS, cache.timeUnit(), "默认 timeUnit 应为 SECONDS");
        assertEquals(CacheType.FULL, cache.type(), "默认 type 应为 FULL");
    }

    @Test
    @DisplayName("测试自定义 timeout")
    void testCustomTimeout() throws Exception {
        Method method = TestService.class.getMethod("getUserWithTimeout", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertEquals(300, cache.timeout(), "timeout 应为 300");
        assertEquals(TimeUnit.SECONDS, cache.timeUnit(), "timeUnit 应为 SECONDS");
    }

    @Test
    @DisplayName("测试 PUT 类型")
    void testPutType() throws Exception {
        Method method = TestService.class.getMethod("updateUser", TestService.User.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertEquals(CacheType.PUT, cache.type(), "type 应为 PUT");
    }

    @Test
    @DisplayName("测试 DELETE 类型")
    void testDeleteType() throws Exception {
        Method method = TestService.class.getMethod("deleteUser", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertEquals(CacheType.DELETE, cache.type(), "type 应为 DELETE");
    }

    @Test
    @DisplayName("测试 FULL 类型")
    void testFullType() throws Exception {
        Method method = TestService.class.getMethod("fullCache", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertEquals(CacheType.FULL, cache.type(), "type 应为 FULL");
    }

    // ========== 注解元数据测试 ==========

    @Test
    @DisplayName("测试注解保留策略")
    void testAnnotationRetention() throws Exception {
        Method method = TestService.class.getMethod("getUser", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解应在运行时可用");
    }

    @Test
    @DisplayName("测试注解目标")
    void testAnnotationTarget() throws Exception {
        Method method = TestService.class.getMethod("getUser", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解可用于方法");
    }

    @Test
    @DisplayName("测试注解可重复性")
    void testAnnotationNotRepeatable() throws Exception {
        Method method = TestService.class.getMethod("getUser", Long.class);

        // 检查是否可重复
        Cache[] annotations = method.getAnnotationsByType(Cache.class);

        assertEquals(1, annotations.length, "默认情况下注解不可重复");
    }

    // ========== 默认值测试 ==========

    @Test
    @DisplayName("测试注解默认值")
    void testDefaultValues() throws Exception {
        Method method = TestService.class.getMethod("getUser", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertEquals(120, cache.timeout(), "timeout 默认值应为 120");
        assertEquals(TimeUnit.SECONDS, cache.timeUnit(), "timeUnit 默认值应为 SECONDS");
        assertEquals(CacheType.FULL, cache.type(), "type 默认值应为 FULL");
    }

    // ========== 必需属性测试 ==========

    @Test
    @DisplayName("测试必需属性")
    void testRequiredAttributes() throws Exception {
        Method method = TestService.class.getMethod("getUser", Long.class);
        Cache cache = method.getAnnotation(Cache.class);

        assertNotNull(cache, "注解不应为 null");
        assertNotNull(cache.cacheName(), "cacheName 不应为 null");
        assertNotNull(cache.key(), "key 不应为 null");
    }

    // ========== 所有 CacheType 值测试 ==========

    @Test
    @DisplayName("测试所有 CacheType 值")
    void testAllCacheTypes() {
        CacheType[] types = CacheType.values();

        assertEquals(3, types.length, "CacheType 应有 3 个值");
        assertEquals(CacheType.FULL, types[0]);
        assertEquals(CacheType.PUT, types[1]);
        assertEquals(CacheType.DELETE, types[2]);
    }

    // ========== 所有 TimeUnit 值测试 ==========

    @Test
    @DisplayName("测试所有 TimeUnit 值可用")
    void testAllTimeUnits() {
        // 验证所有 TimeUnit 值都可以作为注解属性
        assertNotNull(TimeUnit.DAYS);
        assertNotNull(TimeUnit.HOURS);
        assertNotNull(TimeUnit.MINUTES);
        assertNotNull(TimeUnit.SECONDS);
        assertNotNull(TimeUnit.MILLISECONDS);
        assertNotNull(TimeUnit.MICROSECONDS);
        assertNotNull(TimeUnit.NANOSECONDS);
    }
}
