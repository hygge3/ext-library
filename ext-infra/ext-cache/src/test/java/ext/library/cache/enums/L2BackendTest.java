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

        assertEquals(3, values.length, "应有 3 个枚举值");
    }

    @Test
    @DisplayName("测试枚举值存在")
    void testEnumConstants() {
        assertNotNull(L2Backend.AUTO, "AUTO 枚举值应存在");
        assertNotNull(L2Backend.REDIS, "REDIS 枚举值应存在");
        assertNotNull(L2Backend.POSTGRES, "POSTGRES 枚举值应存在");
    }

    @Test
    @DisplayName("测试枚举名称")
    void testEnumNames() {
        assertEquals("AUTO", L2Backend.AUTO.name());
        assertEquals("REDIS", L2Backend.REDIS.name());
        assertEquals("POSTGRES", L2Backend.POSTGRES.name());
    }

    @Test
    @DisplayName("测试 valueOf 方法")
    void testValueOf() {
        assertEquals(L2Backend.AUTO, L2Backend.valueOf("AUTO"));
        assertEquals(L2Backend.REDIS, L2Backend.valueOf("REDIS"));
        assertEquals(L2Backend.POSTGRES, L2Backend.valueOf("POSTGRES"));
    }

    @Test
    @DisplayName("测试枚举顺序")
    void testEnumOrder() {
        L2Backend[] values = L2Backend.values();

        assertEquals(L2Backend.AUTO, values[0], "第一个应为 AUTO");
        assertEquals(L2Backend.REDIS, values[1], "第二个应为 REDIS");
        assertEquals(L2Backend.POSTGRES, values[2], "第三个应为 POSTGRES");
    }

}
