package ext.library.desensitize;

import ext.library.json.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @Sensitive 注解端到端序列化测试
 */
@DisplayName("SensitiveSerializer 脱敏序列化测试")
class SensitiveSerializerTest {

    @Test
    @DisplayName("按策略脱敏字段")
    void testSerializeWithStrategy() {
        String json = JsonUtil.toJson(new User("13812345678", "43012319990101432X"));

        assertTrue(json.contains("138****5678"), "手机号应脱敏，实际：" + json);
        assertTrue(json.contains("4301**********432X"), "身份证应脱敏，实际：" + json);
    }

    @Test
    @DisplayName("无脱敏注解的字段原样输出")
    void testWithoutAnnotation() {
        String json = JsonUtil.toJson(new Plain("13812345678"));

        assertTrue(json.contains("13812345678"), "无注解字段不应脱敏：" + json);
    }

    @Test
    @DisplayName("自定义规则生效")
    void testCustomRule() {
        String json = JsonUtil.toJson(new Custom());

        assertTrue(json.contains("CUSTOM-abc"), "自定义规则应生效，实际：" + json);
    }

    @Test
    @DisplayName("自定义规则实例被缓存，多次序列化结果一致")
    void testCustomRuleCached() {
        User user = new User("13812345678", "43012319990101432X");

        assertEquals(JsonUtil.toJson(user), JsonUtil.toJson(user), "多次序列化结果应一致");
    }

    @Test
    @DisplayName("规则抛异常时输出全掩码，不回退明文")
    void testRuleThrows() {
        String json = JsonUtil.toJson(new Throwing());

        assertTrue(json.contains("******"), "脱敏失败必须输出掩码而非明文，实际：" + json);
        assertTrue(!json.contains("secret"), "绝不能输出明文");
    }

    @Test
    @DisplayName("null 字段不输出")
    void testNullField() {
        String json = JsonUtil.toJson(new User(null, null));

        assertTrue(!json.contains("phone"), "null 字段应被 NON_EMPTY 忽略：" + json);
    }

    @Test
    @DisplayName("非 String 字段上的 @Sensitive 不会脱敏（类型限制）")
    void testNonStringFieldNotDesensitized() {
        String json = JsonUtil.toJson(new NonString());

        assertTrue(json.contains("12345678901"), "Long 字段不受 @Sensitive 保护，实际：" + json);
    }

    @Test
    @DisplayName("自定义规则实例化失败时 fail-closed，输出全掩码而非明文")
    void testUninstantiableRuleFailsClosed() {
        String json = JsonUtil.toJson(new Uninstantiable());

        assertTrue(json.contains("******"), "规则加载失败必须输出掩码而非明文，实际：" + json);
        assertTrue(!json.contains("plaintext"), "绝不能输出明文");
    }

    @Test
    @DisplayName("getter 上的 @Sensitive 同样生效")
    void testSensitiveOnGetter() {
        String json = JsonUtil.toJson(new GetterAnnotated());

        assertTrue(json.contains("138****5678"), "getter 上的 @Sensitive 应生效，实际：" + json);
    }

    @Test
    @DisplayName("只写 @Sensitive 不指定策略时走默认 GENERIC 脱敏")
    void testDefaultStrategyIsGeneric() {
        String json = JsonUtil.toJson(new DefaultStrategy());

        assertTrue(json.contains("1381***5678"), "默认应为前 1/3+后 1/3 掩码，实际：" + json);
        assertTrue(!json.contains("13812345678"), "默认不能输出明文：" + json);
    }

    @Test
    @DisplayName("显式指定 NONE 可跳过脱敏")
    void testExplicitNoneStrategy() {
        String json = JsonUtil.toJson(new ExplicitNone());

        assertTrue(json.contains("nickname-abc"), "显式 NONE 应原样输出：" + json);
    }

    static class User {
        @Sensitive(strategy = DesensitizeStrategy.PHONE)
        private String phone;

        @Sensitive(strategy = DesensitizeStrategy.ID_CARD)
        private String idCard;

        User(String phone, String idCard) {
            this.phone = phone;
            this.idCard = idCard;
        }
    }

    static class Plain {
        public String phone;

        Plain(String phone) {
            this.phone = phone;
        }
    }

    static class DefaultStrategy {
        @Sensitive
        public String remark = "13812345678";
    }

    static class ExplicitNone {
        @Sensitive(strategy = DesensitizeStrategy.NONE)
        public String nickname = "nickname-abc";
    }

    static class Custom {
        @Sensitive(customRule = UpperPrefixRule.class)
        private String value = "abc";
    }

    static class Throwing {
        @Sensitive(customRule = ThrowingRule.class)
        private String value = "secret-value";
    }

    static class NonString {
        @Sensitive(strategy = DesensitizeStrategy.PHONE)
        public Long value = 12345678901L;
    }

    static class Uninstantiable {
        @Sensitive(customRule = NoDefaultConstructorRule.class)
        public String value = "plaintext";
    }

    public static class NoDefaultConstructorRule implements DesensitizeRule {
        public NoDefaultConstructorRule(String required) {
            // 故意不提供无参构造器，触发实例化失败
        }

        @Override
        public Function<String, String> desensitize() {
            return v -> v;
        }
    }

    static class GetterAnnotated {
        private final String phone = "13812345678";

        @Sensitive(strategy = DesensitizeStrategy.PHONE)
        public String getPhone() {
            return phone;
        }
    }

    public static class UpperPrefixRule implements DesensitizeRule {
        @Override
        public Function<String, String> desensitize() {
            return v -> "CUSTOM-" + v;
        }
    }

    public static class ThrowingRule implements DesensitizeRule {
        @Override
        public Function<String, String> desensitize() {
            return v -> {
                throw new IllegalStateException("boom");
            };
        }
    }

}
