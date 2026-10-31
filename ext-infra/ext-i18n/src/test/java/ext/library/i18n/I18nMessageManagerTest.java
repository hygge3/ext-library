package ext.library.i18n;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code ext.i18n.cache-ttl} 必须真正生效：曾只声明不读取，改了翻译要重启才看得见。
 */
class I18nMessageManagerTest {

    private static final Locale ZH_CN = Locale.SIMPLIFIED_CHINESE;

    private final AtomicInteger loads = new AtomicInteger();

    private final TranslationSource source = new TranslationSource() {

        @Override
        public Map<String, String> load(Locale locale) {
            return Map.of("hello", "第" + loads.incrementAndGet() + "版");
        }

        @Override
        public String name() {
            return "stub";
        }

    };

    @Test
    void 未到期时命中缓存() {
        I18nMessageManager manager = new I18nMessageManager(List.of(source), ZH_CN, Duration.ofHours(1));

        assertThat(manager.get("hello")).isEqualTo("第1版");
        assertThat(manager.get("hello")).isEqualTo("第1版");
        assertThat(loads).hasValue(1);
    }

    @Test
    void 到期后重新加载() throws InterruptedException {
        I18nMessageManager manager = new I18nMessageManager(List.of(source), ZH_CN, Duration.ofMillis(20));

        assertThat(manager.get("hello")).isEqualTo("第1版");
        Thread.sleep(100);
        assertThat(manager.get("hello")).isEqualTo("第2版");
    }

    @Test
    void 有效期为零时永不过期() throws InterruptedException {
        I18nMessageManager manager = new I18nMessageManager(List.of(source), ZH_CN, Duration.ZERO);

        assertThat(manager.get("hello")).isEqualTo("第1版");
        Thread.sleep(50);
        assertThat(manager.get("hello")).isEqualTo("第1版");
        assertThat(loads).hasValue(1);
    }

    @Test
    void clear会一并清掉所有来源() {
        I18nMessageManager manager = new I18nMessageManager(List.of(source), ZH_CN, Duration.ofHours(1));
        assertThat(manager.get("hello")).isEqualTo("第1版");

        manager.clear();

        assertThat(manager.get("hello")).isEqualTo("第2版");
    }

    @Test
    void 未命中的键返回键本身() {
        I18nMessageManager manager = new I18nMessageManager(List.of(), ZH_CN, Duration.ofHours(1));

        assertThat(manager.get("missing.key")).isEqualTo("missing.key");
        assertThat(manager.find("missing.key", ZH_CN)).isEmpty();
    }

}
