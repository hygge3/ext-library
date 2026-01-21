package ext.library.json.config;

import ext.library.json.module.ExtJacksonModule;
import ext.library.tool.constant.EmojiSymbol;
import ext.library.tool.runtime.Logs;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;

/**
 * JSON 自动配置
 * <p>
 * 配置 Spring 管理的 JsonMapper，使其与 {@link ext.library.json.util.JsonUtil} 行为一致。
 * <p>
 * 主要配置：
 * <ul>
 *     <li>注册 {@link ExtJacksonModule} 扩展模块</li>
 *     <li>不序列化空值/空集合</li>
 *     <li>忽略未知属性</li>
 *     <li>浮点数反序列化为 BigDecimal</li>
 * </ul>
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JsonAutoConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer extJsonCustomizer() {
        Logs.info(EmojiSymbol.JSON, "载入模块: JSON");
        return builder -> builder
                // 注册扩展模块
                .findAndAddModules().addModules(new ExtJacksonModule())
                // 序列化配置：空对象不报错
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                // 反序列化配置：忽略未知属性
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 反序列化配置：浮点数使用 BigDecimal（避免精度丢失）
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

}
