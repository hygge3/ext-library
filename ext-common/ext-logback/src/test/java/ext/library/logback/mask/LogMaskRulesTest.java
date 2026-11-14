package ext.library.logback.mask;

import ext.library.desensitize.DesensitizeStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("内置脱敏规则")
class LogMaskRulesTest {

    private static final LogMaskRule PASSWORD = LogMaskRules.of(DesensitizeStrategy.PASSWORD);

    @Test
    @DisplayName("键值对只遮蔽值，键名与分隔符保留")
    void keepsKeyName() {
        assertThat(PASSWORD.mask("login password=123456 done")).isEqualTo("login password=****** done");
    }

    @Test
    @DisplayName("JSON 形式的凭据也能遮蔽")
    void masksJsonCredential() {
        String masked = PASSWORD.mask("{\"username\":\"bob\",\"password\":\"p@ss\"}");

        assertThat(masked).contains("\"password\":******").doesNotContain("p@ss");
    }

    @Test
    @DisplayName("token 与 secret 同样遮蔽")
    void masksTokenAndSecret() {
        assertThat(PASSWORD.mask("secret: s3cr3t, accessToken=abc"))
                .isEqualTo("secret: ******, accessToken=******");
    }

    @Test
    @DisplayName("手机号、身份证、邮箱、IP 按各自策略脱敏")
    void masksBuiltInTypes() {
        assertThat(LogMaskRules.of(DesensitizeStrategy.PHONE).mask("call 13812345678 now"))
                .isEqualTo("call 138****5678 now");
        assertThat(LogMaskRules.of(DesensitizeStrategy.EMAIL).mask("mail bob@example.com"))
                .isEqualTo("mail b**@example.com");
        assertThat(LogMaskRules.of(DesensitizeStrategy.IP).mask("from 192.168.2.1"))
                .isEqualTo("from 192.*.*.*");
    }

    @Test
    @DisplayName("无法在自由文本里定位的策略不提供规则")
    void unsupportedStrategy() {
        assertThat(LogMaskRules.of(DesensitizeStrategy.GENERIC)).isNull();
        assertThat(LogMaskRules.of(DesensitizeStrategy.NONE)).isNull();
    }

}
