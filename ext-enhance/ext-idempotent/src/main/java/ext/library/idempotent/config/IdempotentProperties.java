package ext.library.idempotent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等模块配置属性
 *
 * @see KeyStoreType
 */
@ConfigurationProperties(IdempotentProperties.PREFIX)
public class IdempotentProperties {

    public static final String PREFIX = "ext.idempotent";

    /**
     * Key 存储类型，默认为自动检测
     * <p>
     * {@link KeyStoreType#AUTO} 时根据类路径中存在的依赖自动选择，优先级：Redis > PostgreSQL > Memory。
     */
    private KeyStoreType keyStoreType = KeyStoreType.AUTO;

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

}
