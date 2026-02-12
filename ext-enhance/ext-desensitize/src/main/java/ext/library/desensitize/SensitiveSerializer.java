package ext.library.desensitize;

import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.exception.ExtException;
import ext.library.tool.runtime.Logs;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 敏感数据脱敏序列化器
 * <p>
 * 通过 Jackson 的上下文感知机制实现字段级别的脱敏处理
 */
public class SensitiveSerializer extends ValueSerializer<String> {

    private final DesensitizeRule rule;

    public SensitiveSerializer() {
        this.rule = DesensitizeStrategy.NONE;
    }

    public SensitiveSerializer(DesensitizeRule rule) {
        this.rule = rule;
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext ctxt, BeanProperty property) {
        if (property == null) {
            return this;
        }

        // 仅处理 String 类型
        if (!Objects.equals(String.class, property.getType().getRawClass())) {
            return this;
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
            Logs.error(EmojiSymbol.DESENSITIZE, "脱敏失败：{}", e.getMessage());
            gen.writeString(value);
        }
    }

    /**
     * 解析脱敏规则
     * <p>
     * 如果指定了自定义规则（非 NoOpDesensitizeRule），则使用自定义规则；
     * 否则使用策略枚举
     */
    private DesensitizeRule resolveRule(Sensitive annotation) {
        Class<? extends DesensitizeRule> customRuleClass = annotation.customRule();

        // 如果指定了自定义规则（非默认值），则实例化自定义规则
        if (customRuleClass != NoOpDesensitizeRule.class) {
            try {
                return customRuleClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException |
                     InvocationTargetException | NoSuchMethodException e) {
                throw new ExtException(EmojiSymbol.DESENSITIZE, e, "实例化自定义脱敏规则失败：{}" + customRuleClass.getName());
            }
        }

        // 使用策略枚举
        return annotation.strategy();
    }
}
