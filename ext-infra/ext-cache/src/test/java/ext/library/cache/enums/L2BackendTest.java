package ext.library.cache.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * L2Backend 枚举测试
 */
@DisplayName("L2Backend 枚举测试")
class L2BackendTest {

    @Test
    @DisplayName("测试枚举值完整性")
    void testEnumValues() {
        L2Backend[] values = L2Backend.values();

        assertEquals(2, values.length, "应有 2 个枚举值");
    }

    @Test
    @DisplayName("测试枚举值存在")
    void testEnumConstants() {
        assertNotNull(L2Backend.REDIS, "REDIS 枚举值应存在");
        assertNotNull(L2Backend.POSTGRES, "POSTGRES 枚举值应存在");
    }

    @Test
    @DisplayName("测试枚举名称")
    void testEnumNames() {
        assertEquals("REDIS", L2Backend.REDIS.name());
        assertEquals("POSTGRES", L2Backend.POSTGRES.name());
    }

    @Test
    @DisplayName("测试 valueOf 方法")
    void testValueOf() {
        assertEquals(L2Backend.REDIS, L2Backend.valueOf("REDIS"));
        assertEquals(L2Backend.POSTGRES, L2Backend.valueOf("POSTGRES"));
    }

    @Test
    @DisplayName("测试枚举顺序")
    void testEnumOrder() {
        L2Backend[] values = L2Backend.values();

        assertEquals(L2Backend.REDIS, values[0], "第一个应为 REDIS");
        assertEquals(L2Backend.POSTGRES, values[1], "第二个应为 POSTGRES");
    }
}
