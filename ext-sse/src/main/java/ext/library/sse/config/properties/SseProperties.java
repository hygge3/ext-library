package ext.library.sse.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

/**
 * SSE 配置项
 */
@Getter
@Setter
@ConfigurationProperties(SseProperties.PREFIX)
@Validated
public class SseProperties {

    public static final String PREFIX = "ext.sse";

    /** 启用 */
    private Boolean enabled;

    /**
     * 路径
     */
    @Pattern(regexp = "^/(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@/%]*|\\*{1,2})*$")
    private String path;

}