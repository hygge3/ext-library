package ext.library.redis.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存属性
 */
@Data
@ConfigurationProperties(prefix = RedisProperties.PREFIX)
public class RedisProperties {

	public static final String PREFIX = "ext.redis";

	/**
	 * 通用的 key 前缀
	 */
	private String keyPrefix = "";

	/**
	 * redis 锁 后缀
	 */
	private String lockKeySuffix = "locked";

	/**
	 * 默认分隔符
	 */
	private String delimiter = ":";

	/**
	 * 空值标识
	 */
	private String nullValue = "N_V";

	/**
	 * 默认缓存数据的超时时间 (s)
	 */
	private long expireTime = 86400L;

	/**
	 * 默认锁的超时时间 (s)
	 */
	private long defaultLockTimeout = 10L;

}
