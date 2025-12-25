package ext.library.cache.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CacheType 枚举测试
 */
@DisplayName("CacheType 枚举测试")
class CacheTypeTest {

    @Test
    @DisplayName("测试枚举值完整性")
    void testEnumValues() {
        CacheType[] values = CacheType.values();

        assertEquals(3, values.length, "应有 3 个枚举值");
    }

    @Test
    @DisplayName("测试枚举值")
    void testEnumConstants() {
        assertNotNull(CacheType.FULL, "FULL 枚举值应存在");
        assertNotNull(CacheType.PUT, "PUT 枚举值应存在");
        assertNotNull(CacheType.DELETE, "DELETE 枚举值应存在");
    }

    @Test
    @DisplayName("测试枚举名称")
    void testEnumNames() {
        assertEquals("FULL", CacheType.FULL.name());
        assertEquals("PUT", CacheType.PUT.name());
        assertEquals("DELETE", CacheType.DELETE.name());
    }

    @Test
    @DisplayName("测试枚举顺序")
    void testEnumOrder() {
        CacheType[] values = CacheType.values();

        assertEquals(CacheType.FULL, values[0], "第一个应为 FULL");
        assertEquals(CacheType.PUT, values[1], "第二个应为 PUT");
        assertEquals(CacheType.DELETE, values[2], "第三个应为 DELETE");
    }

    @Test
    @DisplayName("测试 valueOf 方法")
    void testValueOf() {
        assertEquals(CacheType.FULL, CacheType.valueOf("FULL"));
        assertEquals(CacheType.PUT, CacheType.valueOf("PUT"));
        assertEquals(CacheType.DELETE, CacheType.valueOf("DELETE"));
    }

    @Test
    @DisplayName("测试枚举在集合中使用")
    void testEnumInCollection() {
        var list = Arrays.asList(CacheType.values());

        assertEquals(3, list.size(), "列表应包含 3 个元素");
    }
}
