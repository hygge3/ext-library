package ext.library.json.util;

import ext.library.tool.exception.ToolException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JsonNodeUtil 测试
 */
@DisplayName("JsonNodeUtil 测试")
class JsonNodeUtilTest {

    /**
     * 测试用 POJO
     */
    public static class User {

        public String name;

        public int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    // ========== parseNode ==========

    @Test
    @DisplayName("parseNode 解析对象")
    void testParseNodeObject() {
        JsonNode node = JsonNodeUtil.parseNode("{\"name\":\"tom\",\"age\":18}");
        assertTrue(node.isObject());
        assertEquals("tom", node.get("name").asString());
    }

    @Test
    @DisplayName("parseNode 入参 null 抛 ToolException")
    void testParseNodeNull() {
        assertThrows(ToolException.class, () -> JsonNodeUtil.parseNode(null));
    }

    @Test
    @DisplayName("parseNode 非法 JSON 抛 ToolException")
    void testParseNodeInvalid() {
        assertThrows(ToolException.class, () -> JsonNodeUtil.parseNode("{not json"));
    }

    // ========== toObj / toList / toNode ==========

    @Test
    @DisplayName("toObj 节点转对象")
    void testToObj() {
        User user = JsonNodeUtil.toObj(JsonNodeUtil.parseNode("{\"name\":\"tom\",\"age\":18}"), User.class);
        assertEquals("tom", user.name);
        assertEquals(18, user.age);
    }

    @Test
    @DisplayName("toObj 字段类型不匹配抛 ToolException")
    void testToObjTypeMismatch() {
        JsonNode node = JsonNodeUtil.parseNode("{\"name\":123,\"age\":\"x\"}");
        assertThrows(ToolException.class, () -> JsonNodeUtil.toObj(node, User.class));
    }

    @Test
    @DisplayName("toList 数组节点转集合")
    void testToList() {
        JsonNode node = JsonNodeUtil.parseNode("[{\"name\":\"a\",\"age\":1},{\"name\":\"b\",\"age\":2}]");
        List<User> users = JsonNodeUtil.toList(node, User.class);
        assertEquals(2, users.size());
        assertEquals("b", users.get(1).name);
    }

    @Test
    @DisplayName("toNode 对象转节点")
    void testToNode() {
        JsonNode node = JsonNodeUtil.toNode(new User("jerry", 30));
        assertEquals("jerry", node.get("name").asString());
        assertEquals(30, node.get("age").asInt());
    }

    // ========== getFieldValue ==========

    @Test
    @DisplayName("getFieldValue 取字段并转换")
    void testGetFieldValue() {
        JsonNode node = JsonNodeUtil.parseNode("{\"age\":\"18\"}");
        assertEquals(18, JsonNodeUtil.getFieldValue(node, "age", Integer.class));
    }

    @Test
    @DisplayName("getFieldValue 字段不存在返回 null")
    void testGetFieldValueMissing() {
        JsonNode node = JsonNodeUtil.parseNode("{\"name\":\"tom\"}");
        assertNull(JsonNodeUtil.getFieldValue(node, "age", Integer.class));
    }

    @Test
    @DisplayName("getFieldValue 字段为 null 返回 null")
    void testGetFieldValueNullNode() {
        JsonNode node = JsonNodeUtil.parseNode("{\"age\":null}");
        assertNull(JsonNodeUtil.getFieldValue(node, "age", Integer.class));
    }

    // ========== 节点工厂 ==========

    @Test
    @DisplayName("创建空节点工厂")
    void testCreateNodes() {
        assertNotNull(JsonNodeUtil.createObjectNode());
        assertNotNull(JsonNodeUtil.createArrayNode());
        assertTrue(JsonNodeUtil.createObjectNode().isEmpty());
        assertTrue(JsonNodeUtil.createArrayNode().isEmpty());
    }

}
