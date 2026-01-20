package ext.library.trans;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.util.List;

/**
 * Bean 序列化修改器
 * <p>
 * 解决 null 值被单独处理的问题，确保 null 值也由 {@link TranslationHandler} 处理。
 */
public class TranslationSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            // 如果序列化器为 TranslationHandler，则将 null 值也交给它处理
            if (writer.getSerializer() instanceof TranslationHandler handler) {
                writer.assignNullSerializer(handler);
            }
        }
        return beanProperties;
    }

}
