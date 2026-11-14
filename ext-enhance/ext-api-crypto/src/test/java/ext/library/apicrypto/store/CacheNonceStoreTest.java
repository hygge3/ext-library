package ext.library.apicrypto.store;

import ext.library.apicrypto.config.ApiCryptoAutoConfiguration;
import ext.library.cache.config.CacheAutoConfiguration;
import ext.library.core.util.SpringUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 基于 ext-cache 的随机数存储测试
 */
@DisplayName("基于 ext-cache 的随机数存储测试")
class CacheNonceStoreTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SpringUtil.class, CacheAutoConfiguration.class,
                    ApiCryptoAutoConfiguration.class));

    @Test
    @DisplayName("装配的是 CacheNonceStore，后端来自 ext-cache")
    void usesCacheStrategy() {
        runner.run(context -> assertThat(context.getBean(NonceStore.class))
                .isInstanceOf(CacheNonceStore.class));
    }

    @Test
    @DisplayName("同一随机数第二次占用失败（防重放）")
    void rejectsReplay() {
        runner.run(context -> {
            NonceStore store = context.getBean(NonceStore.class);
            Duration ttl = Duration.ofSeconds(300);

            assertThat(store.tryAcquire("nonce-once", ttl)).isTrue();
            assertThat(store.tryAcquire("nonce-once", ttl)).isFalse();
            assertThat(store.tryAcquire("nonce-other", ttl)).isTrue();
        });
    }

}
