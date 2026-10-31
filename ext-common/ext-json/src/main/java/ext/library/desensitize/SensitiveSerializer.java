package ext.library.desensitize;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 敏感数据脱敏序列化器
 * <p>
 * 通过 Jackson 的上下文感知机制实现字段级别的脱敏处理
 */
public class SensitiveSerializer extends ValueSerializer<String> {

    /**
     * 自定义规则实例缓存：Class -> 已实例化的规则
     * <p>
     * createContextual 可能被重复调用，反射实例化成本较高；此处缓存避免重复反射。
     */
    private static final Map<Class<? extends DesensitizeRule>, DesensitizeRule> RULE_CACHE = new ConcurrentHashMap<>();

    /** 脱敏失败时的兜底输出（等长全掩码），绝不回退到明文 */
    private static final String FAIL_CLOSED_MASK = "******";

    /** 规则加载失败时的兜底规则：全掩码。绝不降级为 NONE（身份函数，即明文） */
    private static final DesensitizeRule FAIL_CLOSED_RULE = () -> value -> FAIL_CLOSED_MASK;

    private final DesensitizeRule rule;

    public SensitiveSerializer() {
        this.rule = DesensitizeStrategy.NONE;
    }

    public SensitiveSerializer(DesensitizeRule rule) {
        this.rule = Objects.requireNonNull(rule, "rule is null");
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }

        // 非 String 字段无法用 String 脱敏函数处理，返回 null 让 Jackson 回退到默认序列化器。
        // 静默跳过是危险失败模式（开发者以为已脱敏），所以必须告警。
        if (!Objects.equals(String.class, property.getType().getRawClass())) {
            Logs.warn(EmojiSymbol.DESENSITIZE, "@Sensitive 仅支持 String 字段，属性 {} 的类型为 {}，该字段未被脱敏",
                    property.getName(), property.getType());
            return null;
        }

        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (annotation == null) {
            return this;
        }

        return new SensitiveSerializer(resolveRule(annotation));
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        try {
            gen.writeString(rule.desensitize().apply(value));
        } catch (Exception e) {
            // 脱敏失败时绝不能回退到明文，统一输出全掩码。
            // 不记录 e.getMessage()：自定义规则的异常信息常含被处理的原文，会把明文写进日志
            Logs.error(EmojiSymbol.DESENSITIZE, e, "脱敏规则执行失败，字段以全掩码输出：{}", rule.getClass().getName());
            gen.writeString(FAIL_CLOSED_MASK);
        }
    }

    /**
     * 解析脱敏规则
     * <p>
     * 如果指定了自定义规则（非 NoOpDesensitizeRule），则使用自定义规则；
     * 否则使用策略枚举。
     * <p>
     * 自定义规则实例化失败时记错误日志并降级为全掩码规则（fail-closed），
     * 不抛异常阻断整个序列化流程，但<b>绝不降级为 {@link DesensitizeStrategy#NONE}</b>——
     * 规则加载失败恰恰是最该保守的场景，输出明文是不可接受的。
     * <p>
     * 失败结果<b>不写入缓存</b>，避免一次启动期偶发失败（devtools 热重载、NoClassDefFoundError 后恢复）
     * 导致该字段在进程生命周期内永久明文输出。
     */
    private DesensitizeRule resolveRule(Sensitive annotation) {
        Class<? extends DesensitizeRule> customRuleClass = annotation.customRule();

        // 如果指定了自定义规则（非默认值），则实例化自定义规则
        if (customRuleClass != NoOpDesensitizeRule.class) {
            DesensitizeRule cached = RULE_CACHE.get(customRuleClass);
            if (cached != null) {
                return cached;
            }
            DesensitizeRule instantiated = instantiate(customRuleClass);
            // 仅缓存成功加载的规则；instantiate 失败时返回 FAIL_CLOSED_RULE，不入缓存
            if (instantiated != FAIL_CLOSED_RULE) {
                RULE_CACHE.putIfAbsent(customRuleClass, instantiated);
            }
            return instantiated;
        }

        // 使用策略枚举
        return annotation.strategy();
    }

    private static DesensitizeRule instantiate(Class<? extends DesensitizeRule> ruleClass) {
        try {
            return ruleClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            Logs.error(EmojiSymbol.DESENSITIZE, e, "实例化自定义脱敏规则失败，字段以全掩码输出：{}", ruleClass.getName());
            return FAIL_CLOSED_RULE;
        }
    }
}
