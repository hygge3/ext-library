package ext.library.desensitize.handler;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import ext.library.desensitize.annotion.Sensitive;
import ext.library.desensitize.strategy.IDesensitizeRule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 数据脱敏 json 序列化工具
 */
@Slf4j
public class SensitiveHandler extends JsonSerializer<String> implements ContextualSerializer {

    private IDesensitizeRule strategy;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            gen.writeString(strategy.desensitize().apply(value));
        } catch (Exception e) {
            log.error("[😶] 脱敏失败 => {}", e.getMessage());
            gen.writeString(value);
        }
    }

    @SneakyThrows
    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, @NotNull BeanProperty property)
            throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            if (annotation.isCustomRule()) {
                Class<? extends IDesensitizeRule> rule = annotation.customRule();
                this.strategy = rule.getDeclaredConstructor().newInstance();
                return this;
            }
            this.strategy = annotation.strategy();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }

}
