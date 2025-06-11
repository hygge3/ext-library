package ext.library.sse.config.properties;

import jakarta.validation.constraints.Pattern;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * SSE 配置项
 */
@Data
@ConfigurationProperties(SseProperties.PREFIX)
@Validated
public class SseProperties {

    public static final String PREFIX = "ext.sse";

    /** 启用 */
    Boolean enabled;

    /**
     * 路径
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    String path;

}
