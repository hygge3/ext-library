package ext.library.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import org.springframework.context.support.AbstractMessageSource;

/**
 * 把 {@link I18nMessageManager} 接到 Spring 的 {@code MessageSource} 上
 * <p>
 * Thymeleaf 的 {@code #{...}}、Bean Validation 的错误消息、{@code MessageSourceAware} 都走 Spring 的
 * {@code MessageSource}，只提供 {@link I18nMessageManager} 时它们看不到资源文件与数据库里的翻译。
 * <p>
 * 指定语言缺失时回落到 {@code ext.i18n.default-locale}；两边都没有时返回消息键本身
 * （与 {@link I18nMessageManager#get(String, Locale)} 的口径一致），而不是抛异常。
 *
 * @since 4.1.0
 */
public class I18nMessageSource extends AbstractMessageSource {

    private final I18nMessageManager manager;

    public I18nMessageSource(I18nMessageManager manager) {
        this.manager = manager;
        setUseCodeAsDefaultMessage(true);
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        return manager.find(code, locale)
                .or(() -> manager.find(code, manager.defaultLocale()))
                .map(text -> new MessageFormat(text, locale))
                .orElse(null);
    }
}
