package ext.library.lock.config;

import ext.library.lock.enums.LockStoreType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 分布式锁配置属性
 * <p>
 * 配置前缀：{@code ext.lock}
 *
 * @since 4.0.0
 */
@ConfigurationProperties(prefix = LockProperties.PREFIX)
public class LockProperties {

    public static final String PREFIX = "ext.lock";

    /**
     * 锁存储后端，默认为自动检测。AUTO 时按类路径优先 Redis，其次 PostgreSQL；
     * 一个后端都没引时装配阶段直接报错——锁失效比启动失败危险得多
     */
    private LockStoreType storeType = LockStoreType.AUTO;

    public LockStoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(LockStoreType storeType) {
        this.storeType = storeType;
    }

}
