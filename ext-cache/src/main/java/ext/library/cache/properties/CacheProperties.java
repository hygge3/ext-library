package ext.library.cache.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存属性
 */
@Data
@ConfigurationProperties(prefix = CacheProperties.PREFIX)
public class CacheProperties {

    public static final String PREFIX = "ext.cache";

    /**
     * 通用的 key 前缀
     */
    private String keyPrefix = "ext";

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


}