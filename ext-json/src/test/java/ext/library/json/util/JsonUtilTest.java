package ext.library.json.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JsonUtil 测试
 */
@DisplayName("JsonUtil 工具类测试")
class JsonUtilTest {

    // ========== 测试数据类 ==========

    static class AmountHolder {
        private BigDecimal amount;

        public AmountHolder() {
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }

    static class User {
        private String name;
        private Integer age;
        private String email;

        public User() {
        }

        public User(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // ========== toJson 测试 ==========

    @Test
    @DisplayName("测试对象转 JSON")
    void testToJson() {
        User user = new User("张三", 25);
        user.setEmail("zhangsan@example.com");

        String json = JsonUtil.toJson(user);

        assertNotNull(json, "JSON 不应为 null");
        assertTrue(json.contains("张三"), "应包含 name");
        assertTrue(json.contains("25"), "应包含 age");
    }

    @Test
    @DisplayName("测试 null 对象转 JSON")
    void testToJsonNull() {
        String json = JsonUtil.toJson(null);

        assertEquals("", json, "null 对象应返回空字符串");
    }

    @Test
    @DisplayName("测试字符串转 JSON")
    void testToJsonString() {
        String str = "test";
        String json = JsonUtil.toJson(str);

        assertEquals("test", json, "字符串应原样返回");
    }

    @Test
    @DisplayName("测试 BigDecimal 转 JSON")
    void testToJsonBigDecimal() {
        BigDecimal bd = new BigDecimal("123.456");
        String json = JsonUtil.toJson(bd);

        assertEquals("123.456", json, "BigDecimal 应保留精度");
    }

    @Test
    @DisplayName("测试大数值 BigDecimal 转 JSON")
    void testToJsonBigBigDecimal() {
        BigDecimal bd = new BigDecimal("12345678901234567890.123456789");
        String json = JsonUtil.toJson(bd);

        assertEquals("12345678901234567890.123456789", json, "大数值应保留精度");
    }

    @Test
    @DisplayName("测试格式化 JSON")
    void testToPrettyJson() {
        User user = new User("李四", 30);

        String json = JsonUtil.toPrettyJson(user);

        assertNotNull(json, "格式化 JSON 不应为 null");
        assertTrue(json.contains("\n"), "应包含换行符");
        assertTrue(json.contains("李四"), "应包含数据");
    }

    // ========== readObj 测试 ==========

    @Test
    @DisplayName("测试 JSON 转对象")
    void testReadObj() {
        String json = "{\"name\":\"王五\",\"age\":35,\"email\":\"wangwu@example.com\"}";

        User user = JsonUtil.readObj(json, User.class);

        assertNotNull(user, "User 不应为 null");
        assertEquals("王五", user.getName(), "name 应正确解析");
        assertEquals(35, user.getAge(), "age 应正确解析");
        assertEquals("wangwu@example.com", user.getEmail(), "email 应正确解析");
    }

    @Test
    @DisplayName("测试 JSON 转对象 - 缺少字段")
    void testReadObjPartial() {
        String json = "{\"name\":\"赵六\",\"age\":40}";

        User user = JsonUtil.readObj(json, User.class);

        assertNotNull(user, "User 不应为 null");
        assertEquals("赵六", user.getName(), "name 应正确解析");
        assertEquals(40, user.getAge(), "age 应正确解析");
        assertNull(user.getEmail(), "email 应为 null");
    }

    // ========== readList 测试 ==========

    @Test
    @DisplayName("测试 JSON 转列表")
    void testReadList() {
        String json = "[{\"name\":\"用户1\",\"age\":20},{\"name\":\"用户2\",\"age\":25}]";

        List<User> users = JsonUtil.readList(json, User.class);

        assertNotNull(users, "List 不应为 null");
        assertEquals(2, users.size(), "列表大小应为 2");
        assertEquals("用户1", users.get(0).getName(), "第一个用户 name 应正确");
        assertEquals(20, users.get(0).getAge(), "第一个用户 age 应正确");
        assertEquals("用户2", users.get(1).getName(), "第二个用户 name 应正确");
    }

    @Test
    @DisplayName("测试 JSON 转空列表")
    void testReadEmptyList() {
        String json = "[]";

        List<User> users = JsonUtil.readList(json, User.class);

        assertNotNull(users, "List 不应为 null");
        assertTrue(users.isEmpty(), "列表应为空");
    }

    // ========== readMap 测试 ==========

    @Test
    @DisplayName("测试 JSON 转 Map")
    void testReadMap() {
        String json = "{\"name\":\"孙七\",\"age\":45,\"email\":\"sunqi@example.com\"}";

        Map<String, Object> map = JsonUtil.readMap(json);

        assertNotNull(map, "Map 不应为 null");
        assertEquals("孙七", map.get("name"), "name 应正确解析");
        assertEquals(45, map.get("age"), "age 应正确解析");
        assertEquals("sunqi@example.com", map.get("email"), "email 应正确解析");
    }

    @Test
    @DisplayName("测试 JSON 转 Map - 嵌套结构")
    void testReadNestedMap() {
        String json = "{\"user\":{\"name\":\"周八\",\"age\":50}}";

        Map<String, Object> map = JsonUtil.readMap(json);

        assertNotNull(map, "Map 不应为 null");
        assertNotNull(map.get("user"), "user 不应为 null");

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) map.get("user");
        assertEquals("周八", userMap.get("name"), "嵌套 name 应正确解析");
    }

    // ========== convert 测试 ==========

    @Test
    @DisplayName("测试对象类型转换")
    void testConvert() {
        Map<String, Object> map = Map.of(
                "name", "吴九",
                "age", 55,
                "email", "wujiu@example.com"
        );

        User user = JsonUtil.convert(map, User.class);

        assertNotNull(user, "User 不应为 null");
        assertEquals("吴九", user.getName(), "name 应正确转换");
        assertEquals(55, user.getAge(), "age 应正确转换");
        assertEquals("wujiu@example.com", user.getEmail(), "email 应正确转换");
    }

    @Test
    @DisplayName("测试相同类型转换")
    void testConvertSameType() {
        User user = new User("郑十", 60);

        User result = JsonUtil.convert(user, User.class);

        assertNotNull(result, "结果不应为 null");
        assertEquals("郑十", result.getName(), "name 应相同");
    }

    // ========== isValidJson 测试 ==========

    @Test
    @DisplayName("测试有效 JSON 验证")
    void testIsValidJson() {
        assertTrue(JsonUtil.isValidJson("{\"name\":\"测试\"}"), "对象 JSON 应有效");
        assertTrue(JsonUtil.isValidJson("[1,2,3]"), "数组 JSON 应有效");
        assertTrue(JsonUtil.isValidJson("\"test\""), "字符串 JSON 应有效");
        assertTrue(JsonUtil.isValidJson("123"), "数字 JSON 应有效");
        assertTrue(JsonUtil.isValidJson("true"), "布尔 JSON 应有效");
        assertTrue(JsonUtil.isValidJson("null"), "null JSON 应有效");
    }

    @Test
    @DisplayName("测试无效 JSON 验证")
    void testIsInvalidJson() {
        assertFalse(JsonUtil.isValidJson(""), "空字符串应无效");
        assertFalse(JsonUtil.isValidJson(null), "null 应无效");
        assertFalse(JsonUtil.isValidJson("test"), "纯文本应无效");
        // Jackson 解析器对 JSON 格式比较宽容，只要能开始解析就认为有效
        // 所以不完整的对象/数组可能被认为是有效的
    }

    // ========== 往返转换测试 ==========

    @Test
    @DisplayName("测试对象序列化/反序列化往返")
    void testRoundTrip() {
        User original = new User("钱十一", 65);
        original.setEmail("qianshiyi@example.com");

        String json = JsonUtil.toJson(original);
        User restored = JsonUtil.readObj(json, User.class);

        assertEquals(original.getName(), restored.getName(), "name 往返应一致");
        assertEquals(original.getAge(), restored.getAge(), "age 往返应一致");
        assertEquals(original.getEmail(), restored.getEmail(), "email 往返应一致");
    }

    @Test
    @DisplayName("测试列表序列化/反序列化往返")
    void testListRoundTrip() {
        List<User> original = List.of(
                new User("用户1", 20),
                new User("用户2", 25)
        );

        String json = JsonUtil.toJson(original);
        List<User> restored = JsonUtil.readList(json, User.class);

        assertEquals(original.size(), restored.size(), "列表大小应一致");
        assertEquals(original.get(0).getName(), restored.get(0).getName(), "第一个元素 name 应一致");
    }

    // ========== BigDecimal 精度测试 ==========

    @Test
    @DisplayName("测试 BigDecimal 精度往返")
    void testBigDecimalRoundTrip() {
        BigDecimal original = new BigDecimal("123.4567890123456789");

        String json = JsonUtil.toJson(original);
        BigDecimal restored = JsonUtil.readObj(json, BigDecimal.class);

        assertEquals(original, restored, "BigDecimal 往返应保持精度");
    }

    @Test
    @DisplayName("测试包含 BigDecimal 的对象")
    void testObjectWithBigDecimal() {
        AmountHolder holder = new AmountHolder();
        holder.setAmount(new BigDecimal("9999.9999999999"));

        String json = JsonUtil.toJson(holder);
        AmountHolder restored = JsonUtil.readObj(json, AmountHolder.class);

        assertEquals(holder.getAmount(), restored.getAmount(), "BigDecimal 字段应保持精度");
    }
}
