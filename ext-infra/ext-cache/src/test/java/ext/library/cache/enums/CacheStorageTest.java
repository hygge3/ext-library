package ext.library.cache.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CacheStorage 枚举测试
 */
@DisplayName("CacheStorage 枚举测试")
class CacheStorageTest {

    @Test
    @DisplayName("测试枚举值完整性")
    void testEnumValues() {
        CacheStorage[] values = CacheStorage.values();

        assertEquals(5, values.length, "应有 5 个枚举值");
    }

    @Test
    @DisplayName("测试枚举值")
    void testEnumConstants() {
        assertNotNull(CacheStorage.AUTO, "AUTO 枚举值应存在");
        assertNotNull(CacheStorage.CAFFEINE, "CAFFEINE 枚举值应存在");
        assertNotNull(CacheStorage.REDIS, "REDIS 枚举值应存在");
        assertNotNull(CacheStorage.POSTGRES, "POSTGRES 枚举值应存在");
        assertNotNull(CacheStorage.L2, "L2 枚举值应存在");
    }

    @Test
    @DisplayName("测试枚举名称")
    void testEnumNames() {
        assertEquals("AUTO", CacheStorage.AUTO.name());
        assertEquals("CAFFEINE", CacheStorage.CAFFEINE.name());
        assertEquals("REDIS", CacheStorage.REDIS.name());
        assertEquals("POSTGRES", CacheStorage.POSTGRES.name());
        assertEquals("L2", CacheStorage.L2.name());
    }

    @Test
    @DisplayName("测试 valueOf 方法")
    void testValueOf() {
        assertEquals(CacheStorage.AUTO, CacheStorage.valueOf("AUTO"));
        assertEquals(CacheStorage.CAFFEINE, CacheStorage.valueOf("CAFFEINE"));
        assertEquals(CacheStorage.REDIS, CacheStorage.valueOf("REDIS"));
        assertEquals(CacheStorage.POSTGRES, CacheStorage.valueOf("POSTGRES"));
        assertEquals(CacheStorage.L2, CacheStorage.valueOf("L2"));
    }

    @Test
    @DisplayName("测试枚举顺序")
    void testEnumOrder() {
        CacheStorage[] values = CacheStorage.values();

        assertEquals(CacheStorage.AUTO, values[0], "第一个应为 AUTO");
        assertEquals(CacheStorage.CAFFEINE, values[1], "第二个应为 CAFFEINE");
        assertEquals(CacheStorage.REDIS, values[2], "第三个应为 REDIS");
        assertEquals(CacheStorage.POSTGRES, values[3], "第四个应为 POSTGRES");
        assertEquals(CacheStorage.L2, values[4], "第五个应为 L2");
    }

}
