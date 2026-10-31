package ext.library.desensitize.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("脱敏工具")
class DesensitizeUtilTest {

    @Test
    @DisplayName("18 位与 15 位身份证号都遮蔽中间位")
    void masksBothIdCardLengths() {
        assertThat(DesensitizeUtil.maskIdCard("43012319990101432X")).isEqualTo("4301**********432X");
        assertThat(DesensitizeUtil.maskIdCard("430123990101432")).isEqualTo("4301*******1432");
    }

    @Test
    @DisplayName("非身份证长度的输入原样返回")
    void keepsOtherInput() {
        assertThat(DesensitizeUtil.maskIdCard("12345")).isEqualTo("12345");
    }

}
