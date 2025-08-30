package ext.library.idempotent.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等属性配置
 */
@Setter
@Getter
@ConfigurationProperties(IdempotentProperties.PREFIX)
public class IdempotentProperties {

    public static final String PREFIX = "ext.idempotent";

    private KeyStoreType keyStoreType = KeyStoreType.MEMORY;

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