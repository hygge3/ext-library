package ext.library.desensitize.handler;


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import ext.library.desensitize.annotion.Sensitive;
import ext.library.desensitize.strategy.IDesensitizeRule;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import ext.library.tool.exception.ExtException;

import jakarta.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * 数据脱敏 json 序列化工具
 */
public class SensitiveHandler extends JsonSerializer<String> implements ContextualSerializer {

    private IDesensitizeRule strategy;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeString(strategy.desensitize().apply(value));
        } catch (Exception e) {
            Logs.error(EmojiSymbol.DESENSITIZE, "脱敏失败：{}", e.getMessage());
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, @Nonnull BeanProperty property) throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            if (annotation.isCustomRule()) {
                Class<? extends IDesensitizeRule> rule = annotation.customRule();
                try {
                    this.strategy = rule.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new ExtException(EmojiSymbol.DESENSITIZE, e);
                }
                return this;
            }
            this.strategy = annotation.strategy();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }

}
