package ext.library.servlet.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * servlet 基座配置
 *
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = ServletProperties.PREFIX)
public class ServletProperties {

    /** 配置前缀 */
    public static final String PREFIX = "ext.servlet";

    /** 请求体重读配置 */
    private final RepeatableRequest repeatableRequest = new RepeatableRequest();

    public RepeatableRequest getRepeatableRequest() {
        return repeatableRequest;
    }

    /**
     * 请求体重读配置
     */
    public static class RepeatableRequest {

        /**
         * 是否开启请求体缓存过滤器
         * <p>
         * 开启后请求体会被整段缓存到内存，使解密、审计、幂等指纹等多个读取方都能拿到 body；
         * 代价是内存占用，只应在确实存在多个读取方的应用上开启。
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

}
