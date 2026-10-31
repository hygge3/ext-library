package ext.library.trans;

import ext.library.json.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TranslationHandler 序列化翻译测试
 */
@DisplayName("TranslationHandler 字段翻译测试")
class TranslationHandlerTest {

    @BeforeEach
    @AfterEach
    void reset() {
        TranslatorRegistry.clear();
    }

    @Test
    @DisplayName("按类型翻译字段")
    void testTranslateByType() {
        TranslatorRegistry.register("dict", (key, param) -> "DICT-" + key + "-" + param);

        String json = JsonUtil.toJson(new Order("A1", "A1"));

        assertTrue(json.contains("DICT-A1-user_status"), "状态应被翻译，实际：" + json);
        assertTrue(json.contains("\"id\":\"A1\""), "普通字段不受影响：" + json);
    }

    @Test
    @DisplayName("未注册的翻译类型原样输出")
    void testUnknownTranslator() {
        String json = JsonUtil.toJson(new Order("A1", "A1"));

        assertTrue(json.contains("\"status\":\"A1\""), "找不到翻译器应原样输出：" + json);
    }

    @Test
    @DisplayName("多个字段各自翻译，注解不互相覆盖")
    void testMultipleFields() {
        TranslatorRegistry.register("dict", (key, param) -> param + "[" + key + "]");

        String json = JsonUtil.toJson(new Multi("A1", "B2"));

        assertTrue(json.contains("p1[A1]"), "第一个字段翻译错误：" + json);
        assertTrue(json.contains("p2[B2]"), "第二个字段翻译错误：" + json);
    }

    @Test
    @DisplayName("待翻译值为 null 时输出 null")
    void testNullValue() {
        TranslatorRegistry.register("dict", (key, param) -> "never");

        String json = JsonUtil.toJson(new Order(null, null));

        assertTrue(!json.contains("never"), "null 值不应触发翻译：" + json);
    }

    @Test
    @DisplayName("重复注册同类型以最后一次为准")
    void testDuplicateRegister() {
        TranslatorRegistry.register("dict", (key, param) -> "first");
        TranslatorRegistry.register("dict", (key, param) -> "second");

        assertTrue(TranslatorRegistry.get("dict").translate("x", "")
                .equals("second"), "应以后注册的为准");
    }

    @Test
    @DisplayName("翻译器返回 null 时写 null，不抛异常")
    void testTranslatorReturnsNull() {
        TranslatorRegistry.register("dict", (key, param) -> null);

        String json = JsonUtil.toJson(new Order("A1", "A1"));

        assertTrue(json.contains("\"status\":null"), "翻译结果为 null 应写 null，实际：" + json);
    }

    @Test
    @DisplayName("同一次序列化内重复的翻译键只调用一次翻译器")
    void testTranslateCachedPerSerialization() {
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        TranslatorRegistry.register("dict", (key, param) -> "D-" + key + "-" + calls.incrementAndGet());

        String json = JsonUtil.toJson(new java.util.ArrayList<>(java.util.List.of(
                new Order("A1", "A1"), new Order("A1", "A1"), new Order("A1", "A1"))));

        assertEquals(1, calls.get(), "同一键在一次序列化内只应翻译一次，实际：" + json);
        assertTrue(json.contains("D-A1-1"), "结果应复用首次翻译值：" + json);
    }

    @Test
    @DisplayName("注册表只读视图不可修改")
    void testUnmodifiableView() {
        TranslatorRegistry.register("dict", (key, param) -> "x");
        try {
            TranslatorRegistry.getTranslators().clear();
            throw new AssertionError("只读视图不应可修改");
        } catch (UnsupportedOperationException expected) {
            assertEquals(1, TranslatorRegistry.getTranslators().size());
        }
    }

    static class Order {
        public String id;

        @Translate(type = "dict", param = "user_status")
        public String status;

        Order(String id, String status) {
            this.id = id;
            this.status = status;
        }
    }

    static class Multi {
        @Translate(type = "dict", param = "p1")
        public String fieldA;

        @Translate(type = "dict", param = "p2")
        public String fieldB;

        Multi(String a, String b) {
            this.fieldA = a;
            this.fieldB = b;
        }
    }

}
