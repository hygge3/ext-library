package ext.library.json.serializer;

import ext.library.json.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用 {@code longValue()} 判范围会把超出 long 的大数直接截断（{@code 2^64} 截成 0），
 * 当成安全值输出裸数字，前端照样丢精度。
 */
@DisplayName("大数序列化")
class BigNumberSerializerTest {

    @Test
    @DisplayName("超出 JS 安全整数范围输出字符串")
    void unsafeIntegersAsString() {
        assertThat(JsonUtil.toJson(BigInteger.TWO.pow(64))).isEqualTo("\"18446744073709551616\"");
        assertThat(JsonUtil.toJson(Long.MAX_VALUE)).isEqualTo("\"9223372036854775807\"");
    }

    @Test
    @DisplayName("安全整数范围内输出数字")
    void safeIntegersAsNumber() {
        assertThat(JsonUtil.toJson(123L)).isEqualTo("123");
        assertThat(JsonUtil.toJson(BigInteger.valueOf(9007199254740991L))).isEqualTo("9007199254740991");
    }

}
