package ext.library.security.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用 RFC 6238 附录 B 的测试向量校验实现（RFC 里的 secret 是 ASCII "12345678901234567890"，
 * 8 位结果取后 6 位即为本库的 6 位验证码）。
 */
@DisplayName("TOTP 工具")
class TotpUtilTest {

    private static final String RFC_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @Test
    @DisplayName("RFC 6238 测试向量")
    void matchesRfcVector() {
        assertThat(TotpUtil.generateCode(RFC_SECRET, 59L)).isEqualTo("287082");
        assertThat(TotpUtil.generateCode(RFC_SECRET, 1111111109L)).isEqualTo("081804");
        assertThat(TotpUtil.generateCode(RFC_SECRET, 1111111111L)).isEqualTo("050471");
        assertThat(TotpUtil.generateCode(RFC_SECRET, 1234567890L)).isEqualTo("005924");
        assertThat(TotpUtil.generateCode(RFC_SECRET, 2000000000L)).isEqualTo("279037");
        assertThat(TotpUtil.generateCode(RFC_SECRET, 20000000000L)).isEqualTo("353130");
    }

    @Test
    @DisplayName("当前时间片内通过，错误码与位数不对的码拒绝")
    void verifyWindow() {
        long now = System.currentTimeMillis() / 1000;
        String current = TotpUtil.generateCode(RFC_SECRET, now);

        assertThat(TotpUtil.verify(RFC_SECRET, current, 0)).isTrue();
        assertThat(TotpUtil.verify(RFC_SECRET, current, 1)).isTrue();
        assertThat(TotpUtil.verify(RFC_SECRET, "000000", 1)).isFalse();
        assertThat(TotpUtil.verify(RFC_SECRET, "12345", 1)).isFalse();
    }

    @Test
    @DisplayName("secret 容忍小写、空格与填充符")
    void tolerantSecret() {
        long now = System.currentTimeMillis() / 1000;
        String code = TotpUtil.generateCode(RFC_SECRET, now);
        assertThat(TotpUtil.verify("gezd gnbv gy3t qojq gezd gnbv gy3t qojq==", code, 1)).isTrue();
    }

    @Test
    @DisplayName("生成的 secret 是 32 位 Base32，且能立刻用于校验")
    void generateSecret() {
        String secret = TotpUtil.generateSecret();
        assertThat(secret).hasSize(32).matches("[A-Z2-7]+");

        long now = System.currentTimeMillis() / 1000;
        assertThat(TotpUtil.verify(secret, TotpUtil.generateCode(secret, now), 1)).isTrue();
    }

    @Test
    @DisplayName("绑定 URI 带 issuer 与 secret")
    void buildUri() {
        String uri = TotpUtil.buildUri("ext library", "alice@example.com", RFC_SECRET);
        assertThat(uri).startsWith("otpauth://totp/ext%20library:alice%40example.com?secret=" + RFC_SECRET)
            .contains("&issuer=ext%20library")
            .contains("&digits=6")
            .contains("&period=30");
    }

}
