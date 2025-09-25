package ext.library.idempotent.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等属性配置
 */
@ConfigurationProperties(IdempotentProperties.PREFIX)
public class IdempotentProperties {

    public static final String PREFIX = "ext.idempotent";

    private KeyStoreType keyStoreType = KeyStoreType.MEMORY;

    public KeyStoreType getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(KeyStoreType keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * 存储方式
     */
    public enum KeyStoreType {

        /**
         * 内存存储
         */
        MEMORY,
        /**
         * redis 存储
         */
        REDIS

    }

}