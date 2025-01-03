package ext.library.sse.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSE 配置项
 */
@Data
@ConfigurationProperties(SseProperties.PREFIX)
public class SseProperties {

	public static final String PREFIX = "ext.sse";

	/** 启用 */
	private Boolean enabled;

	/**
	 * 路径
	 */
	private String path;

}
