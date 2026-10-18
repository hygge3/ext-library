package ext.library.ratelimit.config;

import ext.library.ratelimit.store.RateLimitStore;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流模块配置属性
 *
 * @see RateLimitStore.Type
 */
@ConfigurationProperties(RateLimitProperties.PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "ext.ratelimit";

    /**
     * 限流存储类型，默认为自动检测
     * <p>
     * {@code AUTO} 时根据类路径中存在的依赖自动选择，优先级：Redis > Memory。
     * PostgreSQL 需显式指定，因为它还需要数据源才能工作。
     */
    private RateLimitStore.Type storeType = RateLimitStore.Type.AUTO;

    public RateLimitStore.Type getStoreType() {
        return storeType;
    }

    public void setStoreType(RateLimitStore.Type storeType) {
        this.storeType = storeType;
    }

}
