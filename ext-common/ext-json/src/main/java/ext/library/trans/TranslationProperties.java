package ext.library.trans;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 翻译模块配置
 * <p>
 * 前缀 {@code ext.trans}
 *
 * @since 4.1.0
 */
@ConfigurationProperties(prefix = TranslationProperties.PREFIX)
public class TranslationProperties {

    /**
     * 配置前缀
     */
    public static final String PREFIX = "ext.trans";

    /**
     * 是否启用
     * <p>
     * 关闭后不扫描注册 {@link Translator}，{@link Translate} 注解的字段按原值输出。
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
