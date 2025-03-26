package ext.library.ratelimiter.config.properties;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置
 */
@Getter
@Setter
@ConfigurationProperties(prefix = LimiterProperties.PREFIX)
public class LimiterProperties implements Serializable {

	/**
	 * 配置前缀
	 */
	public static final String PREFIX = "ext.limiter";

	/**
	 * 开启限流
	 */
	 boolean enable;

}
