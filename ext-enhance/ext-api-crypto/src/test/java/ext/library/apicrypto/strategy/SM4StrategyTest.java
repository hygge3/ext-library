package ext.library.apicrypto.strategy;

import ext.library.crypto.SM4Util;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SM4 加密策略")
class SM4StrategyTest {

    private final SM4Strategy strategy = new SM4Strategy();

    @Test
    @DisplayName("CBC 模式加解密往返")
    void roundTrip() {
        String key = SM4Util.generateKey(128);
        String plainText = "{\"userId\":1,\"name\":\"张三\"}";

        String encrypted = strategy.encrypt(key, plainText, null);

        assertThat(strategy.decrypt(key, encrypted, null)).isEqualTo(plainText);
    }

    @Test
    @DisplayName("相同明文每次得到不同密文")
    void cipherTextDiffersPerRequest() {
        String key = SM4Util.generateKey(128);

        String first = strategy.encrypt(key, "same", null);
        String second = strategy.encrypt(key, "same", null);

        assertThat(first).isNotEqualTo(second);
    }

}
