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
     * Key 存储类型，默认为内存存储
     */
    private KeyStoreType keyStoreType = KeyStoreType.MEMORY;

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

}
