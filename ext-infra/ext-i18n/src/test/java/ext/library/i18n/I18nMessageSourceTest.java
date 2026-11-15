package ext.library.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Spring MessageSource 适配：模板 {@code #{...}} 与校验消息都从这里取翻译
 */
@DisplayName("MessageSource 适配测试")
class I18nMessageSourceTest {

    private static I18nMessageSource source() {
        TranslationSource file = new TranslationSource() {
            @Override
            public Map<String, String> load(Locale locale) {
                return switch (locale.getLanguage()) {
                    case "zh" -> Map.of("login.title", "登录", "mail.left", "还剩 {0} 次");
                    case "en" -> Map.of("login.title", "Sign in");
                    default -> Map.of();
                };
            }

            @Override
            public String name() {
                return "test";
            }

            @Override
            public int order() {
                return 10;
            }
        };
        I18nMessageManager manager = new I18nMessageManager(List.of(file), Locale.SIMPLIFIED_CHINESE,
                Duration.ZERO);
        return new I18nMessageSource(manager);
    }

    @Test
    void resolvesByLocale() {
        I18nMessageSource source = source();

        assertEquals("登录", source.getMessage("login.title", null, Locale.SIMPLIFIED_CHINESE));
        assertEquals("Sign in", source.getMessage("login.title", null, Locale.US));
    }

    @Test
    void fallsBackToDefaultLocale() {
        // en 没有 mail.left，回落到默认语言的中文
        assertEquals("还剩 3 次", source().getMessage("mail.left", new Object[]{3}, Locale.US));
    }

    @Test
    void missingKeyReturnsCode() {
        assertEquals("no.such.key", source().getMessage("no.such.key", null, Locale.SIMPLIFIED_CHINESE));
    }
}
