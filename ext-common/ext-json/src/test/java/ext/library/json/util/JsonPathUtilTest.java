package ext.library.json.util;

import com.jayway.jsonpath.ReadContext;
import ext.library.tool.exception.ToolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JsonPathUtil 测试
 */
@DisplayName("JsonPathUtil 测试")
class JsonPathUtilTest {

    private static final String JSON = """
            {"store":{"book":[{"title":"A","price":8.95},{"title":"B","price":12.99}],"name":"shop"}}""";

    // ========== parse ==========

    @Test
    @DisplayName("parse 合法 JSON")
    void testParse() {
        ReadContext context = JsonPathUtil.parse(JSON);
        assertEquals("shop", JsonPathUtil.read(context, "$.store.name"));
    }

    @Test
    @DisplayName("parse 非法 JSON 抛 ToolException")
    void testParseInvalidJson() {
        assertThrows(ToolException.class, () -> JsonPathUtil.parse("{not json"));
    }

    // ========== read ==========

    @Test
    @DisplayName("read 单值")
    void testRead() {
        assertEquals("A", JsonPathUtil.read(JSON, "$.store.book[0].title"));
    }

    @Test
    @DisplayName("read 列表")
    void testReadList() {
        assertEquals(2, ((java.util.List<?>) JsonPathUtil.read(JSON, "$.store.book")).size());
    }

    @Test
    @DisplayName("read 路径不存在抛 ToolException")
    void testReadPathNotFound() {
        assertThrows(ToolException.class, () -> JsonPathUtil.read(JSON, "$.store.missing"));
    }

    @Test
    @DisplayName("read 非法路径抛 ToolException")
    void testReadInvalidPath() {
        assertThrows(ToolException.class, () -> JsonPathUtil.read(JSON, "$..["));
    }

    @Test
    @DisplayName("read 复用 ReadContext 结果一致")
    void testReadReuseContext() {
        ReadContext context = JsonPathUtil.parse(JSON);
        assertEquals("A", JsonPathUtil.read(context, "$.store.book[0].title"));
        assertEquals("B", JsonPathUtil.read(context, "$.store.book[1].title"));
    }

    // ========== hasPath ==========

    @Test
    @DisplayName("hasPath 存在返回 true")
    void testHasPathTrue() {
        assertTrue(JsonPathUtil.hasPath(JSON, "$.store.book[0].title"));
    }

    @Test
    @DisplayName("hasPath 不存在返回 false")
    void testHasPathFalse() {
        assertFalse(JsonPathUtil.hasPath(JSON, "$.store.missing"));
    }

    // ========== readOrDefault ==========

    @Test
    @DisplayName("readOrDefault 路径存在返回实际值")
    void testReadOrDefaultHit() {
        assertEquals("A", JsonPathUtil.readOrDefault(JSON, "$.store.book[0].title", "none"));
    }

    @Test
    @DisplayName("readOrDefault 路径不存在返回默认值")
    void testReadOrDefaultMiss() {
        assertEquals("none", JsonPathUtil.readOrDefault(JSON, "$.store.missing", "none"));
    }

    @Test
    @DisplayName("readOrDefault 值为 null 返回默认值")
    void testReadOrDefaultNullValue() {
        assertEquals("none", JsonPathUtil.readOrDefault("{\"a\":null}", "$.a", "none"));
    }

    // ========== 路径缓存 ==========

    @Test
    @DisplayName("同一路径多次读取走缓存且结果一致")
    void testPathCacheConsistency() {
        for (int i = 0; i < 3; i++) {
            assertEquals("shop", JsonPathUtil.read(JSON, "$.store.name"));
        }
    }

    @Test
    @DisplayName("非法路径不进缓存且抛 ToolException")
    void testInvalidPathNotCached() {
        assertThrows(ToolException.class, () -> JsonPathUtil.read(JSON, "$..["));
        // 缓存中不应残留，后续合法路径仍可用
        assertEquals("shop", JsonPathUtil.read(JSON, "$.store.name"));
    }

}
